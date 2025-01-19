import tensorflow as tf
import numpy as np
import pandas as pd
from sklearn.preprocessing import StandardScaler
import os

# Define the path for the TFLite model
output_path = os.path.join(os.path.dirname(__file__), "../app/src/main/assets/")
os.makedirs(output_path, exist_ok=True)

def load_real_data(csv_path):
    if not os.path.exists(csv_path):
        return generate_training_data()

    df = pd.read_csv(csv_path)

    # Add derived features
    df['games_experience'] = np.log1p(df['games_played'])
    df['score_consistency'] = df['consistency'] * df['avg_score']

    # Features
    feature_cols = ['games_played', 'win_rate', 'avg_score',
                   'consistency', 'games_experience', 'score_consistency']
    X = df[feature_cols].values

    # Labels
    y = df[['pred_win_rate', 'consistency', 'improvement', 'play_style']].values

    return X, y

def normalize_data(X):
    scaler = StandardScaler()
    X_scaled = scaler.fit_transform(X)
    return X_scaled

def generate_training_data(n_samples=1000):
    # Generate features (now 6 features to match the model)
    X = np.random.rand(n_samples, 6)
    X[:, 0] *= 100  # games_played (0-100)
    X[:, 1] *= 1.0  # win_rate (0-1)
    X[:, 2] *= 1000  # avg_score (0-1000)
    X[:, 3] *= 1.0  # consistency (0-1)
    X[:, 4] = np.log1p(X[:, 0])  # games_experience
    X[:, 5] = X[:, 2] * X[:, 3]  # score_consistency

    # Generate labels
    y = np.zeros((n_samples, 4))
    y[:, 0] = 0.6 * X[:, 1] + 0.4 * X[:, 3]  # predicted win rate
    y[:, 1] = X[:, 3]  # consistency
    y[:, 2] = 1.0 - (X[:, 0] / 100)  # improvement potential
    y[:, 3] = 0.4 * X[:, 1] + 0.6 * X[:, 2] / 1000  # play style

    return X, y

def create_model():
    model = tf.keras.Sequential([
        tf.keras.layers.Dense(12, activation='relu', input_shape=(6,)),
        tf.keras.layers.Dropout(0.2),
        tf.keras.layers.Dense(24, activation='relu'),
        tf.keras.layers.Dropout(0.2),
        tf.keras.layers.Dense(12, activation='relu'),
        tf.keras.layers.Dense(4, activation='sigmoid')
    ])

    model.compile(
        optimizer=tf.keras.optimizers.Adam(learning_rate=0.001),
        loss='mse',
        metrics=['mae', 'mse']
    )
    return model

# Main execution
if __name__ == "__main__":
    csv_path = "training_data.csv"

    # Load or generate data
    X, y = load_real_data(csv_path)

    # Normalize features
    X = normalize_data(X)

    # Split data
    train_size = int(0.8 * len(X))
    X_train, X_val = X[:train_size], X[train_size:]
    y_train, y_val = y[:train_size], y[train_size:]

    # Create and train model
    model = create_model()
    model.fit(
        X_train, y_train,
        validation_data=(X_val, y_val),
        epochs=50,
        batch_size=32,
        verbose=1
    )

    # Convert to TFLite
    converter = tf.lite.TFLiteConverter.from_keras_model(model)
    tflite_model = converter.convert()

    # Save model
    
    with tf.io.gfile.GFile(os.path.join(output_path, 'statistics_model.tflite'), 'wb') as f:
        f.write(tflite_model)

    print(f"Model saved to {output_path}")

    # Test prediction
    test_input = np.array([[50, 0.7, 500, 0.8, np.log1p(50), 400]])  # Example input with 6 features
    test_input = normalize_data(test_input)  # Normalize test input
    predictions = model.predict(test_input)
    print("\nTest prediction:", predictions)