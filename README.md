# RPG Cam

A client-side Fabric mod for Minecraft 26.3 that adds a smooth third-person swivel camera.

## What it does

Press **F5** to enter rear third-person view, then move your mouse to look around freely. Movement follows the camera's direction, while your character turns toward where you're moving.

- Sprint in any direction while roaming.
- Aim with a visible crosshair. Attacking or interacting smoothly turns your character toward the target.
- Enjoy smooth head movement, with a choice of camera-following or movement-based head pitch.
- Adjust camera distance and position to get the view you like.
- Get an overhead view of your character while sleeping.

The camera moves closer around obstacles and gently zooms back out when there's room. First-person and front-facing third-person keep their normal controls. RPG Cam also pauses its swivel controls while riding, flying with elytra, or taking a panorama.

## Settings

Open **Mods → RPG Cam → Configure** with Mod Menu installed. You can also assign **Open RPG Cam Settings** under **Options → Controls → Key Binds → RPG Cam**; it has no key assigned by default.

The settings let you enable or disable the mod and adjust camera smoothing, character turn speed, camera offsets, distance, and head movement. For head rotation, **Realistic** keeps trying to look toward the camera, while **Folded** brings the head back toward forward when the camera moves behind the character.

## Installation

Requires **Java 25**, **Fabric Loader 0.19.5 or newer**, and **Fabric API** for Minecraft 26.3. Mod Menu is optional.

Put `rpg-cam-1.0.0.jar` in your client's `mods` folder. RPG Cam is entirely client-side, so nothing needs to be installed on the server.

## Building

Run `./gradlew build`. The JARs appear in `build/libs/`. GitHub Actions also provides them as build artifacts.

## License

[Creative Commons Attribution-NonCommercial 4.0 International (CC BY-NC 4.0)](https://creativecommons.org/licenses/by-nc/4.0/).
