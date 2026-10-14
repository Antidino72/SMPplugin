const fs = require('fs');
const source = "C:/Users/antidino/Desktop/SMPplugin/target/smpplugin-1.0-SNAPSHOT.jar";
const destination = "C:/Users/antidino/AppData/Local/JetBrains/IntelliJIdea2026.2/com.nickawrist.MinecraftDevServer/SMP/plugins/smpplugin-1.0-SNAPSHOT.jar";

fs.copyFile(source, destination, (err) => {
    if (err) throw err;
    console.log('Fichier copié avec succès dans le serveur de test !');
});