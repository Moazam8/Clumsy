import { Directory, Encoding, Filesystem } from "@capacitor/filesystem";
import { Share } from "@capacitor/share";

window.ClumsyNativeBridge = {
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
