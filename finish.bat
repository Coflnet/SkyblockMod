powershell -window hidden -Command
taskkill /F /IM javaw.exe
taskkill /F /IM Minecraft.exe
timeout /t 1
START C:\XboxGames\"Minecraft Launcher"\Content\Minecraft.exe
exit