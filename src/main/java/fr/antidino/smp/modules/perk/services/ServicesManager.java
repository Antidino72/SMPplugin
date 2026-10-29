package fr.antidino.smp.modules.perk.services;

import fr.antidino.smp.CorePlugin;

public class ServicesManager {
    public NightVisionService nightVisionService;
    public MagnetService magnetService;
    public DoubleJumpService doubleJumpService;
    private final CorePlugin plugin;
    public ServicesManager(CorePlugin plugin) {
        this.plugin = plugin;
        this.doubleJumpService = new DoubleJumpService(plugin);
        this.magnetService = new MagnetService(plugin);
        this.nightVisionService = new NightVisionService();
    }

}
