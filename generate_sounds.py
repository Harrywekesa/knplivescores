import wave
import struct
import math
import os

def generate_tone(filename, frequency, duration_ms, volume=0.5, sample_rate=44100):
    num_samples = int(sample_rate * (duration_ms / 1000.0))
    
    with wave.open(filename, 'w') as wav_file:
        wav_file.setnchannels(1)
        wav_file.setsampwidth(2)
        wav_file.setframerate(sample_rate)
        
        for i in range(num_samples):
            # Calculate sample value
            t = float(i) / sample_rate
            value = int(volume * 32767.0 * math.sin(2.0 * math.pi * frequency * t))
            
            # Pack as 16-bit signed integer (little-endian)
            data = struct.pack('<h', value)
            wav_file.writeframesraw(data)

def generate_goal_horn():
    path = os.path.join('app', 'src', 'main', 'res', 'raw', 'goal_horn.wav')
    # Goal horn is typically a loud, low frequency chord (e.g. 150 Hz + 200 Hz + 250 Hz)
    # We'll just generate a simple loud 200 Hz tone for 2 seconds to simulate a horn
    generate_tone(path, 200.0, 2000, 0.8)
    print(f"Generated {path}")

def generate_whistle():
    path = os.path.join('app', 'src', 'main', 'res', 'raw', 'whistle.wav')
    # Whistle is typically a short, high frequency sound (e.g. 2500 Hz)
    # 500ms short burst
    generate_tone(path, 2500.0, 500, 0.6)
    print(f"Generated {path}")

if __name__ == '__main__':
    generate_goal_horn()
    generate_whistle()
