import { registerPlugin } from "@capacitor/core";
import { Directory, Encoding, Filesystem } from "@capacitor/filesystem";
import { Share } from "@capacitor/share";

const GoogleDriveAuth = registerPlugin("GoogleDriveAuth");
const BiometricUnlock = registerPlugin("BiometricUnlock");

window.ClumsyNativeBridge = {
  async authorizeDrive() {
    return GoogleDriveAuth.authorize();
  },
  async biometricUnlockAvailable() {
    const result = await BiometricUnlock.isAvailable();
    return result.available;
  },
  async hasBiometricUnlock() {
    const result = await BiometricUnlock.hasSavedPassword();
    return result.enabled;
  },
  async unlockWithBiometrics() {
    return BiometricUnlock.unlockPassword();
  },
  async saveBiometricPassword(password) {
    return BiometricUnlock.savePassword({ password });
  },
  async clearBiometricPassword() {
    return BiometricUnlock.clearPassword();
  },
  async shareTextFile({ filename, content }) {
    const { uri } = await Filesystem.writeFile({
      path: filename,
      data: content,
      directory: Directory.Cache,
      encoding: Encoding.UTF8
    });

    await Share.share({
      title: filename,
      dialogTitle: "Save or share from Clumsy",
      files: [uri]
    });
  }
};
