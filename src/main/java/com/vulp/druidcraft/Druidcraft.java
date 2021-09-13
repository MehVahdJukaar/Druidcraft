package com.vulp.druidcraft;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.vulp.druidcraft.config.Configuration;
import com.vulp.druidcraft.network.PacketHandler;
import com.vulp.druidcraft.registry.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.biome.provider.NetherBiomeProvider;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

@Mod("druidcraft")
public class Druidcraft {
    public static Druidcraft INSTANCE;
    public static final String MODID = "druidcraft";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public Druidcraft() {
        INSTANCE = this;

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Configuration.common_config);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Configuration.client_config);

        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        bus.addListener(this::setup);
        bus.addListener(this::doClientStuff);
        bus.addListener(this::doParticleStuff);

        Configuration.loadConfig(Configuration.common_config, FMLPaths.CONFIGDIR.get().resolve("druidcraft-common.toml").toString());
        Configuration.loadConfig(Configuration.client_config, FMLPaths.CONFIGDIR.get().resolve("druidcraft-client.toml").toString());
    }

    private void setup(final FMLCommonSetupEvent event) {
        PacketHandler.init();

        event.enqueueWork(() -> {
            // Registry.register(Registry.BIOME_PROVIDER_CODEC, new ResourceLocation(MODID, "druidcraft"), DruidcraftNetherBiomeProvider.CODEC);
            // TODO: Pull the list from NetherBiomeProvider somehow and then overwrite with the same list with custom biomes tacked onto the end.
            /*NetherBiomeProvider.Preset.DEFAULT_NETHER_PROVIDER_PRESET = new NetherBiomeProvider.Preset(new ResourceLocation("nether"), (preset, lookupRegistry, seed) -> {
                return new NetherBiomeProvider(seed, ImmutableList.of(Pair.of(new Biome.Attributes(0.0F, 0.0F, 0.0F, 0.0F, 0.0F), () -> {
                    return lookupRegistry.getOrThrow(Biomes.NETHER_WASTES);
                }), Pair.of(new Biome.Attributes(0.0F, -0.5F, 0.0F, 0.0F, 0.0F), () -> {
                    return lookupRegistry.getOrThrow(Biomes.SOUL_SAND_VALLEY);
                }), Pair.of(new Biome.Attributes(0.4F, 0.0F, 0.0F, 0.0F, 0.0F), () -> {
                    return lookupRegistry.getOrThrow(Biomes.CRIMSON_FOREST);
                }), Pair.of(new Biome.Attributes(0.0F, 0.5F, 0.0F, 0.0F, 0.375F), () -> {
                    return lookupRegistry.getOrThrow(Biomes.WARPED_FOREST);
                }), Pair.of(new Biome.Attributes(-0.5F, 0.0F, 0.0F, 0.0F, 0.175F), () -> {
                    return lookupRegistry.getOrThrow(Biomes.BASALT_DELTAS);
                }), Pair.of(new Biome.Attributes(0.4F, 0.5F, 0.0F, 0.0F, 0.0F), () -> {
                    return lookupRegistry.getOrThrow(BiomeRegistry.BiomeKeys.torrid_jungle);
                })), Optional.of(Pair.of(lookupRegistry, preset)));
            });*/
            VanillaIntegrationRegistry.setup();
        });

        // Deferred to static methods
        /*MinecraftForge.EVENT_BUS.register(new EventHandler());
        MinecraftForge.EVENT_BUS.register(new BiomeLoadEventHandler());*/
        LOGGER.info("Setup method registered.");
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        RenderRegistry.registryRenders(event);
        // RenderHandler.registerColors();
        LOGGER.info("Client registry method registered.");
    }

    private void doParticleStuff(final ParticleFactoryRegisterEvent event) {
        ParticleRegistry.registerFactories();
        LOGGER.info("Particle registry method registered.");
    }
}
