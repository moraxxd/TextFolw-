import logging
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from .config import settings
from .api.v1.routes import router as api_v1_router

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(name)s: %(message)s"
)
logger = logging.getLogger("textflow.main")

app = FastAPI(
    title=settings.APP_NAME,
    version=settings.VERSION,
    description=(
        "TextFlow Self-Hosted Backend: Zero-Commercial-API Speech Recognition & "
        "Translation Engine specializing in Indigenous & Tribal Languages (Gondi, Kolami) "
        "and National Indian Languages (Hindi, Marathi, English) using open-source models."
    ),
    docs_url="/docs",
    redoc_url="/redoc"
)

# CORS configuration to allow local app testing and cross-origin access
app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.ALLOWED_ORIGINS,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(api_v1_router)

@app.get("/")
async def root():
    return {
        "app": settings.APP_NAME,
        "version": settings.VERSION,
        "status": "online",
        "documentation": "/docs",
        "tribal_languages_supported": ["Gondi (gon)", "Kolami (kfb)"],
        "target_languages": ["English (en)", "Marathi (mr)"]
    }

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("app.main:app", host=settings.HOST, port=settings.PORT, reload=settings.DEBUG)
