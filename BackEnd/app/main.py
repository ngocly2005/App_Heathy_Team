from fastapi import FastAPI

from app.core.config import get_settings
from app.auth import router as auth_router
from app.profile import router as profile_router
from app.tracking import router as tracking_router
from app.ai import router as ai_router

settings = get_settings()
app = FastAPI(title=settings.app_name, version=settings.app_version, description="Backend")
app.include_router(auth_router, prefix=settings.api_v1_prefix)
app.include_router(profile_router, prefix=settings.api_v1_prefix)
app.include_router(tracking_router, prefix=settings.api_v1_prefix)
app.include_router(ai_router, prefix=settings.api_v1_prefix)

@app.get("/health", tags=["system"])
def health_check() -> dict[str, str]:
    return {"status": "ok", "app_name": settings.app_name, "environment": settings.environment}
