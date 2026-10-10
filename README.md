# RPG Cam

A client-side Fabric mod for Minecraft 26.3 with a third-person swivel camera.

## How it works

Press **F5** to switch to third-person rear view. Move the mouse to orbit the camera freely. WASD moves relative to the camera, and your character turns toward the movement direction instead of following the mouse. Turning the camera while standing still keeps your character facing the same direction.

First-person and front-facing third-person use Minecraft's normal controls. The swivel camera also pauses while riding, sleeping, or flying with elytra, and resumes when returning to ordinary movement. Movement retains Minecraft's normal sneak/item-use speed reductions. Sprint-jump momentum follows the movement direction even while the character is still turning.

## Settings

Install **Mod Menu 21.0.0** for Minecraft 26.3, then open **Mods → RPG Cam → Configure**. Mod Menu is optional; without it, edit `config/rpg-cam.json` while Minecraft is closed.

| Setting | Range | Default | Behavior |
| --- | --- | --- | --- |
| Mod Enabled | On / Off | On | Enables the swivel camera in third-person rear view. |
| Smoothing Multiplier | 0.1–1.0 | 0.5 | Lower values smooth camera rotation more; 1.0 responds immediately. |
| Player Turn Speed | 0–10 | 5 | Higher values turn the character faster; 0 keeps its facing fixed while movement remains camera-relative. |
| Camera Y Offset | 0.0–2.0, step 0.1 | 0.0 | Raises the camera's pivot in world space. |
| Camera X Offset | −1.0–1.0, step 0.1 | 0.0 | Shifts the camera sideways; positive is right. |

Settings save immediately. Camera rotation smoothing is independent of frame rate. Camera distance still uses Minecraft's collision checks, and the pivot offsets are clipped against obstacles.

This release changes the camera and movement only. Attacks, item use, and interaction targeting retain Minecraft's character-facing behavior; they do not turn the character to match the orbit camera.

## Installation and building

Requires **Java 25**, **Fabric Loader 0.19.5 or newer**, and **Fabric API** for Minecraft 26.3. Put the built `rpg-cam-1.0.0.jar` in your client's `mods` folder. Nothing is required on the server.

```sh
./gradlew build
```

The mod jar is written to `build/libs/`. GitHub Actions builds pushes and pull requests and uploads the jars as the **Artifacts** download.

## License

CC0-1.0.
