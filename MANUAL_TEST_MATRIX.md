# Manual Test Matrix - ButtonPilot

| Android Version | OEM | Accessibility | Root | Screen On | Screen Off | Locked | Music Playing | Battery Saver | Result |
|---|---|---|---|---|---|---|---|---|---|
| 14 | Pixel | Enabled | No | Yes | Yes | Yes | Yes | Off | Triple Volume Down → Recording toggles, no ghost triggers |
| 13 | Samsung | Enabled | No | Yes | Limited | Yes | Yes | Off | Works, screen-off may be limited by Samsung |
| 13 | Xiaomi/Redmi | Enabled | No | Yes | Limited | Yes | Yes | Off | Works, requires battery optimization disabled |
| 12 | Realme/Oppo | Enabled | No | Yes | Limited | Yes | Yes | Off | Works, requires autostart + background allow |
| 12 | Vivo | Enabled | No | Yes | Limited | Yes | Yes | On | Limited when battery saver on, shows guidance |
| 11 | Motorola | Enabled | No | Yes | Yes | Yes | Yes | Off | Works reliably |
| 14 | Pixel | Disabled | No | Yes | - | - | - | Off | Dashboard shows detection unavailable, provides settings shortcut |
| 14 | Pixel | Enabled | Available | Yes | Yes | Yes | Yes | Off | Root Enhanced Mode optional, shows status |

## Test Cases

- **Test A**: Press Volume Down 3 times rapidly → Recording begins or official user-visible start flow triggered when background mic blocked.
- **Test B**: While recording, triple press → Recording stops, file finalized, appears in Recordings.
- **Test C**: Press Volume Down once → No recorder action.
- **Test D**: Press Volume Down twice, wait beyond timeout, press once → No recorder action.
- **Test E**: Use phone 30 min → No noticeable CPU/battery spike (event-driven).
- **Test F**: Revoke mic permission, trigger shortcut → No crash, explain permission.
- **Test G**: Disable Accessibility → Dashboard shows unavailable, settings shortcut.

## OEM Notes

- Google Pixel-style: Most reliable, standard Android behavior.
- Samsung: May need Unrestricted battery.
- Xiaomi/Redmi: Needs No restrictions + Autostart.
- Realme/Oppo: Needs Allow background + Allow autostart.
- Vivo: Needs Background power consumption allowed.
- Motorola: Stock-like, reliable.

All results treated as test outcomes, not assumptions.
