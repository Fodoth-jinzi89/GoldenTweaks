@echo off
REM Start Headroom proxy for GoldenTweaks DeepCode (DeepSeek upstream).
REM DeepCode routes via .deepcode/settings.json BASE_URL=http://127.0.0.1:8787
REM Usage: script\headroom-proxy.bat  (Ctrl+C to stop)

headroom proxy --port 8787 --openai-api-url https://api.deepseek.com/v1
