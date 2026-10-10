# RPG Cam

A client-side Fabric mod for Minecraft 26.3 with a third-person swivel camera.

## How it works

Press **F5** to switch to third-person rear view. Move the mouse to orbit the camera freely. WASD moves relative to the camera, and your character turns toward the movement direction instead of following the mouse. Hold your sprint key to sprint forward, backward, sideways, or diagonally while roaming in rear third-person view. Directional double-tap sprinting also works. Minecraft's normal hunger, crouching, item-use, water, and collision restrictions still apply.

Turning the camera while standing still keeps your character's horizontal facing unchanged. In Head Rotation: Camera mode, the head smoothly follows camera yaw and pitch with a ±75° horizontal limit while the body keeps its movement-facing rotation. Directional mode smoothly tilts the head with vertical velocity and keeps its horizontal facing aligned with the body. Head smoothing uses Smoothing Multiplier. A 180°/second head-yaw speed limit prevents left/right snaps even at smoothing 1.0.

First-person and front-facing third-person use Minecraft's normal controls. The swivel camera also pauses while riding, sleeping, or flying with elytra, and resumes when returning to ordinary movement. Movement retains Minecraft's normal sneak/item-use speed reductions. Sprint-jump momentum follows the movement direction even while the character is still turning.

## Settings

Install **Mod Menu 21.0.0** for Minecraft 26.3, then open **Mods → RPG Cam → Configure**. You can also bind **Open RPG Cam Settings** under **Options → Controls → Key Binds → RPG Cam** to open settings directly while playing, even with the mod disabled. It is unbound by default and works without Mod Menu. Mod Menu is optional; without a binding or Mod Menu, edit `config/rpg-cam.json` while Minecraft is closed.

| Setting | Range | Default | Behavior |
| --- | --- | --- | --- |
| Mod Enabled | On / Off | On | Enables the swivel camera in third-person rear view. |
| Smoothing Multiplier | 0.1–1.0 | 0.5 | Lower values smooth camera rotation more; 1.0 responds immediately. |
| Player Turn Speed | 0–10 | 3.5 | Higher values turn the character faster; 0 keeps its facing fixed while movement remains camera-relative. |
| Camera Y Offset | 0.0–2.0, step 0.1 | 0.2 | Raises the camera's pivot in world space. |
| Camera X Offset | −1.0–1.0, step 0.1 | 0.5 | Shifts the camera sideways; positive is right. |
| Realistic Head | On / Off | On | In Camera mode, On tracks the camera within ±75° and smoothly crosses between sides. Off folds rear-facing camera angles back toward forward: a 180° difference becomes 0°. |
| Camera Distance | 2.0–15.0, step 0.1 | 4.0 | Requested camera distance in blocks, with smooth distance changes and normal obstruction checks. |
| Head Rotation | Camera / Directional | Camera | Camera smoothly follows camera yaw and pitch. Directional smoothly tilts the head up while rising, down while falling, and forward at rest/on the ground, based on vertical velocity. |

Toggle states show green ON and red OFF. Head Rotation and Realistic Head share a row; Camera Distance occupies the bottom row. Directional changes only the visible head pitch; body yaw still follows movement and tool/projectile aiming still follows the cursor. Settings save immediately. Camera rotation smoothing is independent of frame rate. Camera distance glides toward its target independently of frame rate, including when an obstruction clears or the distance slider changes. It pulls inward immediately when necessary to prevent clipping through blocks. Camera distance and pivot offsets retain Minecraft's normal visual-shape obstruction checks.

The crosshair appears in rear third-person view and targets along the rendered camera's center ray, including camera offsets. Attacking, swinging, mining, and interacting smoothly turn your character toward the cursor using Player Turn Speed. The visible character eases toward the aim while swinging or using an item, including when releasing a bow. Tool and projectile aiming still uses the exact cursor direction while the visible turn catches up. Movement remains camera-relative during these actions. Right-clicking empty space or an unreachable/obstructed target does not turn the character unless an item is successfully used (for example, firing a projectile).

Targeting retains normal player interaction reach and checks that the player can see the target, so camera offsets do not grant extra reach or interactions through walls. All code and hooks run only on the client; multiplayer actions use Minecraft's existing packets.

Existing saved settings retain their values. To use the new defaults, adjust them in Mod Menu or remove `config/rpg-cam.json` while Minecraft is closed.

## Installation and building

Requires **Java 25**, **Fabric Loader 0.19.5 or newer**, and **Fabric API** for Minecraft 26.3. Put the built `rpg-cam-1.0.0.jar` in your client's `mods` folder. Nothing is required on the server.

```sh
./gradlew build
```

The mod jar is written to `build/libs/`. GitHub Actions builds pushes and pull requests and uploads the jars as the **Artifacts** download.

## License

[Creative Commons Attribution-NonCommercial 4.0 International (CC BY-NC 4.0)](https://creativecommons.org/licenses/by-nc/4.0/).
