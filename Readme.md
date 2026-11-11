# Broadcast Tech Tools 1.0

Android app with **seven** offline RF calculators: Pi attenuator, T attenuator,
dBm to watts, watts to dBm, VSWR to return loss, return loss to VSWR, and wavelength
in a medium. English and Spanish are supported. The larger tool collection is a
future phase; it is not part of this version.

## Inputs and limits

- Use a decimal point or comma, without thousands separators. Scientific notation
  is also accepted when entered or pasted. Inputs must be finite numbers.
- Attenuation: >= 0 dB; impedance: > 0 ohms. At 0 dB the ideal resistor network
  is shown explicitly as shorts (0 ohms) and open circuits (infinite resistance).
- Power in watts: > 0. VSWR: >= 1. Return loss: >= 0 dB.
- VSWR 1 represents a perfect match (infinite return loss). Return loss 0 dB
  represents total reflection (infinite VSWR).
- Frequency: > 0 MHz. Velocity factor: > 0 and <= 1.
- Numerically unrepresentable results are rejected with an error instead of
  presenting an overflow or underflow as a physical result. Results use nine
  significant digits; a displayed coefficient near a limiting value is rounded.

Each tool keeps its draft inputs and calculated result while switching tools or
languages. Saved instance state restores these during Activity recreation.
Language preference persists; calculation history is not stored permanently.

## Build and regression checks

Requires Java 17, Gradle 8.9, Android SDK platform 35 and build tools 34.0.0.
Run `gradle :app:verifyCalculators` for the calculation/state regression checks.
`gradle assembleDebug` runs those checks before building the APK. The checked-in
GitHub Actions workflow uploads the debug APK as `Broadcast-Tech-Tools-APK`.

The regression checks execute the production Java calculation and state classes,
including physical network checks and round trips. They are not emulator/UI tests.
The APK is debug-signed, not a Play Store release. A debug APK built on a fresh
runner can have a different signing key; updating over an older installation then
requires matching keys or uninstalling the older debug app first.
