#!/bin/bash
# =============================================================================
# CareerCompass AI - Deployment Script
# =============================================================================
# This script guides you through deploying the full stack to Railway.
#
# Prerequisites:
#   - Railway CLI installed (npm i -g @railway/cli)
#   - Railway account with PostgreSQL provisioned
#
# Usage:
#   chmod +x deploy.sh
#   ./deploy.sh
#
# Or deploy manually:
#   1. Push to GitHub
#   2. Connect repo to Railway
#   3. Add PostgreSQL database
#   4. Set environment variables
# =============================================================================

set -e

echo "🚀 CareerCompass AI - Deployment Script"
echo "========================================"
echo ""

# Check if Railway CLI is installed
if ! command -v railway &> /dev/null; then
    echo "⚠️  Railway CLI not found."
    echo ""
    echo "To deploy manually:"
    echo "  1. Push your code to GitHub:"
    echo "     git push origin main"
    echo ""
    echo "  2. Go to https://railway.app and create a new project"
    echo ""
    echo "  3. Add a PostgreSQL database:"
    echo "     + New → Database → PostgreSQL"
    echo ""
    echo "  4. Deploy the backend:"
    echo "     + New → GitHub Repo → Select CarrerAi"
    echo "     Railway auto-detects Spring Boot from pom.xml"
    echo "     Set env vars: CORS_ALLOWED_ORIGINS, GEMINI_API_KEY, JWT_SECRET"
    echo ""
    echo "  5. Deploy the frontend:"
    echo "     + New → GitHub Repo → Select CarrerAi"
    echo "     Root directory: frontend"
    echo "     Set env var: VITE_API_BASE_URL=https://your-backend.railway.app/api"
    echo ""
    echo "  6. Update CORS on backend:"
    echo "     Set CORS_ALLOWED_ORIGINS=https://your-frontend.railway.app"
    echo ""
    echo "🎉 Your app is live!"
    exit 0
fi

echo "✅ Railway CLI found!"
echo ""
echo "Deploying backend..."
railway up --service backend

echo ""
echo "Deploying frontend..."
railway up --service frontend

echo ""
echo "🎉 Deployment complete!"
echo "Check your Railway dashboard for service URLs."
