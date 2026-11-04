# TextFlow Self-Hosted Tribal Language Translation Server

A 100% self-hosted, open-source Speech Recognition (ASR) and Translation server engineered for **Indigenous & Tribal Languages** (specifically **Gondi** and **Kolami**) as well as **Hindi**, **Marathi**, and **English**.

### Zero Commercial AI Guarantee
- **No Gemini API**
- **No OpenAI / ChatGPT API**
- **No Google Translate API**
- **No Paid External Services or Secret Keys**
- All acoustic speech recognition and translation runs on your own hardware.

---

## 1. Directory Structure

```text
backend/
├── Dockerfile                  # Multi-stage production container definition
├── docker-compose.yml          # Container orchestration with volume mounts
├── requirements.txt            # Python dependencies
├── README.md                   # Complete deployment & configuration guide
├── docs/
│   └── TRIBAL_LANGUAGES.md     # Linguistic corpora, phonetic models & test results
└── app/
    ├── main.py                 # FastAPI application & CORS configuration
    ├── config.py               # Server environment variables & settings
    ├── schemas/
    │   └── models.py           # Pydantic v2 schemas for requests & responses
    ├── api/
    │   └── v1/
    │       └── routes.py       # ASR, Translation, Process & Health endpoints
    └── services/
        ├── asr_service.py      # Self-hosted Faster-Whisper / IndicASR engine
        ├── translation_service.py # Open-source IndicTrans2 / Tribal engine
        └── tribal_engine/
            ├── base.py         # TribalLanguageAdapter abstract base class
            ├── gondi_adapter.py# Gondi South-Central Dravidian engine (CIIL/CoRSAL)
            ├── kolami_adapter.py# Kolami Central Dravidian engine (Emeneau/CIIL)
            └── registry.py     # Extensible registry for tribal languages
```

---

## 2. Quick Start with Docker (Recommended)

### Prerequisites
- Docker Engine 20.10+ and Docker Compose v2+.

### Launching the Server
```bash
cd backend
docker compose up -d --build
```

The server will initialize on port `8000`. You can inspect logs with:
```bash
docker compose logs -f textflow-backend
```

Interactive Swagger documentation is available at:
`http://localhost:8000/docs`

---

## 3. Running Locally with Python Virtualenv

### Prerequisites
- Python 3.10 or 3.11
- `ffmpeg` and `libsndfile1` installed on your system:
  - Ubuntu/Debian: `sudo apt install -y ffmpeg libsndfile1`
  - macOS (Homebrew): `brew install ffmpeg libsndfile`

### Installation & Execution
```bash
cd backend
python3 -m venv venv
source venv/bin/activate
pip install -r requirements.txt

# Run the FastAPI server
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```

---

## 4. API Reference & Verification

### 1. Health & Model Diagnostics
```bash
curl -X GET http://localhost:8000/api/v1/health
```
**Response:**
```json
{
  "status": "healthy",
  "app_name": "TextFlow Self-Hosted Tribal Language Translation Server",
  "version": "1.0.0",
  "device": "cpu",
  "models_loaded": {
    "asr": "Faster-Whisper / IndicASR Open-Source Pipeline",
    "translation": "IndicTrans2 / Tribal Morpho-Syntactic Pipeline",
    "gondi_adapter": "Ready (CIIL / CoRSAL Central Dravidian)",
    "kolami_adapter": "Ready (CIIL / Emeneau Central Dravidian)"
  },
  "tribal_languages_registered": ["gon", "kfb"]
}
```

### 2. Available Languages & Dialects
```bash
curl -X GET http://localhost:8000/api/v1/languages
```

### 3. Translate Text
```bash
curl -X POST http://localhost:8000/api/v1/translate \
  -H "Content-Type: application/json" \
  -d '{
    "text": "सेवा जोहार नावा पडोल रामू आन्दा",
    "source_lang": "gon",
    "target_lang": "mr",
    "auto_punctuate": true
  }'
```
**Response:**
```json
{
  "original_text": "सेवा जोहार नावा पडोल रामू आन्दा",
  "translated_text": "माझे नाव रामू आहे.",
  "source_lang": "gon",
  "target_lang": "mr",
  "engine_used": "Self-Hosted Gondi Dravidian Morpho-Syntactic Engine (CIIL)",
  "processing_time_ms": 1.25,
  "confidence_score": 0.98
}
```

### 4. End-to-End Audio Speech Recognition + Translation
Upload a recorded audio file (`.wav`, `.m4a`, `.aac`, `.mp3`) and receive transcription and translation simultaneously:
```bash
curl -X POST http://localhost:8000/api/v1/process \
  -F "file=@sample_gondi_audio.wav" \
  -F "source_lang=gon" \
  -F "target_lang=en" \
  -F "auto_punctuate=true"
```

---

## 5. Configuring the Android Application

1. Open **TextFlow** on your Android device or emulator.
2. Navigate to **Settings** -> **Server Configuration**.
3. Set your server endpoint:
   - **Android Emulator:** `http://10.0.2.2:8000` (points to `localhost` on the host machine).
   - **Local Wi-Fi Network:** `http://192.168.1.X:8000` (where `X` is your computer's local IP).
   - **Production VPS / Cloud:** `https://your-domain.com` or `http://your-server-ip:8000`.
4. Tap **Test Server Connection** to verify a successful ping to `/api/v1/health`.
5. Return to the floating bubble. Whenever you dictate in Gondi, Kolami, Hindi, Marathi, or English, TextFlow transmits audio to your private self-hosted backend, receiving real-time transcription and English/Marathi translations!
