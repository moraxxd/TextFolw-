import os
from pydantic_settings import BaseSettings
from typing import List

class Settings(BaseSettings):
    APP_NAME: str = "TextFlow Self-Hosted Tribal Language Translation Server"
    VERSION: str = "1.0.0"
    DEBUG: bool = False
    HOST: str = "0.0.0.0"
    PORT: int = 8000
    
    # Allowed CORS origins
    ALLOWED_ORIGINS: List[str] = ["*"]
    
    # Audio Upload Limits
    MAX_AUDIO_SIZE_BYTES: int = 25 * 1024 * 1024  # 25 MB
    ALLOWED_AUDIO_EXTENSIONS: List[str] = [".wav", ".mp3", ".m4a", ".aac", ".ogg", ".flac"]
    
    # Open-Source Model Weights Directories
    MODELS_DIR: str = os.getenv("MODELS_DIR", "/app/models")
    WHISPER_MODEL_SIZE: str = os.getenv("WHISPER_MODEL_SIZE", "base")
    INDICTRANS2_MODEL_PATH: str = os.getenv("INDICTRANS2_MODEL_PATH", "/app/models/indictrans2")
    
    # Computation Device: 'cpu', 'cuda', 'auto'
    DEVICE: str = os.getenv("DEVICE", "cpu")
    COMPUTE_TYPE: str = os.getenv("COMPUTE_TYPE", "int8")

    class Config:
        env_file = ".env"

settings = Settings()
