package com.tacz.guns.client.event;

import com.google.gson.*;
import com.tacz.guns.GunMod;
import com.tacz.guns.client.resource.pojo.PackInfo;
import com.tacz.guns.resource.PackMeta;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.commons.lang3.time.StopWatch;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;


@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class PlayerEnterWorld {
    private static final Path FOLDER = Paths.get("config", GunMod.MOD_ID, "custom");
    private static final Pattern PACK_INFO_PATTERN = Pattern.compile("^(\\w+)/pack\\.json$");
    public static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    @SubscribeEvent
    public static void onPlayerEnterWorld(PlayerEvent.PlayerLoggedInEvent event) {
        File[] files = FOLDER.toFile().listFiles();
        if (files != null && files.length > 0){
            Component component = Component.literal("[TACZ] 发现旧版枪包资源，是否进行转换? ").append(Component.literal("[点我转换]").withStyle(
                    Style.EMPTY.withColor(ChatFormatting.GREEN)
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tacz convert"))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("点击转换"))
            )));
            event.getEntity().sendSystemMessage(component);
        }
    }

    public static void convert() {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        Path resourcePacksPath = FMLPaths.GAMEDIR.get().resolve("tacz");
        File folder = resourcePacksPath.toFile();
        if (!folder.isDirectory()) {
            try {
                Files.createDirectories(folder.toPath());
            } catch (Exception e) {
                player.sendSystemMessage(Component.literal("[TACZ] 初始化枪包文件夹失败! 请检查日志!"));
                GunMod.LOGGER.error("Failed to init tacz directory...", e);
                return;
            }
        }

        File[] files = FOLDER.toFile().listFiles();
        int cnt = 0;
        if (files != null && files.length > 0) {
            StopWatch watch = StopWatch.createStarted();
            {
                player.sendSystemMessage(Component.literal("[TACZ] 开始转换，请勿关闭游戏..."));
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".zip")) {
                        LegacyPack pack = fromZipFile(file);
                        if (pack != null) {
                            player.sendSystemMessage(Component.literal("[TACZ] 正在尝试转换旧版枪包: " + file.getName()));
                            try {
                                pack.convert();
                            } catch (FileAlreadyExistsException e) {
                                player.sendSystemMessage(Component.literal("[TACZ] 目标文件已存在! 跳过..."));
                                continue;
                            }
                            cnt++;
                            player.sendSystemMessage(Component.literal("[TACZ] " + file.getName() + " 转换完成! "));
                        }
                    }
                }
            }
            watch.stop();
            double time = watch.getTime(TimeUnit.MICROSECONDS) / 1000.0;
            player.sendSystemMessage(Component.literal("[TACZ] 转换结束! 总耗时: "+ time +" ms. 总计枪包: " +cnt+ "个. 请重启游戏以加载新的枪包资源!"));
        }
    }

    public static LegacyPack fromZipFile(File file) {
        try (ZipFile zipFile = new ZipFile(file)) {
            var iteration = zipFile.entries();
            while (iteration.hasMoreElements()) {
                String path = iteration.nextElement().getName();
                Matcher matcher = PACK_INFO_PATTERN.matcher(path);

                if (matcher.find()) {
                    String namespace = matcher.group(1);
                    ZipEntry entry = zipFile.getEntry(path);
                    try (InputStream stream = zipFile.getInputStream(entry)) {
                        PackInfo info = GSON.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), PackInfo.class);
                        if (info != null) {
                            return new LegacyPack(file, namespace, info);
                        }
                    } catch (IOException | JsonSyntaxException | JsonIOException exception) {
                        GunMod.LOGGER.warn(exception.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public record LegacyPack(File file, String namespace, PackInfo info) {
        private static final Pattern DISPLAY_PATTERN = Pattern.compile("^(\\w+)/(\\w+)/display/([\\w/.]+)$");
        private static final Pattern INDEX_PATTERN = Pattern.compile("^(\\w+)/(\\w+)/index/([\\w/.]+)$");
        private static final Pattern DATA_PATTERN = Pattern.compile("^(\\w+)/(\\w+)/data/([\\w/.]+)$");
        private static final Pattern MODELS_PATTERN = Pattern.compile("^(\\w+)/models/([\\w/.]+)$");
        private static final Pattern LANG_PATTERN = Pattern.compile("^(\\w+)/lang/([\\w/.]+)$");
        private static final Pattern ANIMATION_PATTERN = Pattern.compile("^(\\w+)/animations/([\\w/.]+)$");
        private static final Pattern TEXTURE_PATTERN = Pattern.compile("^(\\w+)/textures/([\\w/.]+)$");
        private static final Pattern SOUND_PATTERN = Pattern.compile("^(\\w+)/sounds/([\\w/.]+)$");
        private static final Pattern PLAYER_ANIMATOR_PATTERN = Pattern.compile("^(\\w+)/player_animator/([\\w/.]+)$");
        private static final Pattern TAGS_PATTERN = Pattern.compile("^(\\w+)/tags/([\\w/.]+)$");
        private static final Pattern RECIPE_PATTERN = Pattern.compile("^(\\w+)/recipes/([\\w/.]+)$");
        private static final Pattern PACK_INFO = Pattern.compile("^(\\w+)/pack\\.json$");

        private boolean parsePackInfo(ZipOutputStream newZip, ZipEntry entry, ZipFile oldPack) throws IOException {
            Matcher matcher = PACK_INFO.matcher(entry.getName());
            if (matcher.find()) {
                String namespace = matcher.group(1);
                String newPath = "assets/" + namespace + "/gunpack_info.json";
                writeEntry(newZip, entry, oldPack, newPath);
                return true;
            }
            return false;
        }

        private boolean parseRecipe(ZipOutputStream newZip, ZipEntry entry, ZipFile oldPack) throws IOException {
            Matcher matcher = RECIPE_PATTERN.matcher(entry.getName());
            if (matcher.find()) {
                String namespace = matcher.group(1);
                String path = matcher.group(2);
                String newPath = "data/" + namespace + "/recipes/" + path;

                try (InputStream stream = oldPack.getInputStream(entry)) {
                    JsonObject object = GSON.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), JsonObject.class);
                    if (object != null) {
                        object.addProperty("type", "tacz:gun_smith_table_crafting");
                        newZip.putNextEntry(new ZipEntry(newPath));
                        newZip.write(GSON.toJson(object).getBytes());
                        newZip.closeEntry();
                    }
                } catch (JsonParseException e) {
                    GunMod.LOGGER.warn("Failed to parse recipe to new style: {}", entry.getName());
                }
                return true;
            }
            return false;
        }

        private boolean parseTags(ZipOutputStream newZip, ZipEntry entry, ZipFile oldPack) throws IOException {
            Matcher matcher = TAGS_PATTERN.matcher(entry.getName());
            if (matcher.find()) {
                String namespace = matcher.group(1);
                String path = matcher.group(2);
                String newPath = "data/" + namespace + "/tacz_tags/" + path;
                writeEntry(newZip, entry, oldPack, newPath);
                return true;
            }
            return false;
        }

        private boolean parsePlayerAnimator(ZipOutputStream newZip, ZipEntry entry, ZipFile oldPack) throws IOException {
            Matcher matcher = PLAYER_ANIMATOR_PATTERN.matcher(entry.getName());
            if (matcher.find()) {
                String namespace = matcher.group(1);
                String path = matcher.group(2);
                String newPath = "assets/" + namespace + "/player_animator/" + path;
                writeEntry(newZip, entry, oldPack, newPath);
                return true;
            }
            return false;
        }

        private boolean parseSound(ZipOutputStream newZip, ZipEntry entry, ZipFile oldPack) throws IOException {
            Matcher matcher = SOUND_PATTERN.matcher(entry.getName());
            if (matcher.find()) {
                String namespace = matcher.group(1);
                String path = matcher.group(2);
                String newPath = "assets/" + namespace + "/tacz_sounds/" + path;
                writeEntry(newZip, entry, oldPack, newPath);
                return true;
            }
            return false;
        }

        private boolean parseTexture(ZipOutputStream newZip, ZipEntry entry, ZipFile oldPack) throws IOException {
            Matcher matcher = TEXTURE_PATTERN.matcher(entry.getName());
            if (matcher.find()) {
                String namespace = matcher.group(1);
                String path = matcher.group(2);
                String newPath = "assets/" + namespace + "/textures/" + path;
                writeEntry(newZip, entry, oldPack, newPath);
                return true;
            }
            return false;
        }

        private boolean parseAnimation(ZipOutputStream newZip, ZipEntry entry, ZipFile oldPack) throws IOException {
            Matcher matcher = ANIMATION_PATTERN.matcher(entry.getName());
            if (matcher.find()) {
                String namespace = matcher.group(1);
                String path = matcher.group(2);
                String newPath = "assets/" + namespace + "/animations/" + path;
                writeEntry(newZip, entry, oldPack, newPath);
                return true;
            }
            return false;
        }

        private boolean parseLang(ZipOutputStream newZip, ZipEntry entry, ZipFile oldPack) throws IOException {
            Matcher matcher = LANG_PATTERN.matcher(entry.getName());
            if (matcher.find()) {
                String namespace = matcher.group(1);
                String path = matcher.group(2);
                String newPath = "assets/" + namespace + "/lang/" + path;
                writeEntry(newZip, entry, oldPack, newPath);
                return true;
            }
            return false;
        }

        private boolean parseModels(ZipOutputStream newZip, ZipEntry entry, ZipFile oldPack) throws IOException {
            Matcher matcher = MODELS_PATTERN.matcher(entry.getName());
            if (matcher.find()) {
                String namespace = matcher.group(1);
                String path = matcher.group(2);
                String newPath = "assets/" + namespace + "/geo_models/" + path;
                writeEntry(newZip, entry, oldPack, newPath);
                return true;
            }
            return false;
        }

        private boolean parseDisplay(ZipOutputStream newZip, ZipEntry entry, ZipFile oldPack) throws IOException {
            Matcher matcher = DISPLAY_PATTERN.matcher(entry.getName());
            if (matcher.find()) {
                String namespace = matcher.group(1);
                String type = matcher.group(2);
                String path = matcher.group(3);
                String newPath = "assets/" + namespace + "/display/" + type + "/" + path;
                writeEntry(newZip, entry, oldPack, newPath);
                return true;
            }
            return false;
        }

        private boolean parseIndex(ZipOutputStream newZip, ZipEntry entry, ZipFile oldPack) throws IOException {
            Matcher matcher = INDEX_PATTERN.matcher(entry.getName());
            if (matcher.find()) {
                String namespace = matcher.group(1);
                String type = matcher.group(2);
                String path = matcher.group(3);
                String newPath = "data/" + namespace + "/index/" + type + "/" + path;
                writeEntry(newZip, entry, oldPack, newPath);
                return true;
            }
            return false;
        }

        private boolean parseData(ZipOutputStream newZip, ZipEntry entry, ZipFile oldPack) throws IOException {
            Matcher matcher = DATA_PATTERN.matcher(entry.getName());
            if (matcher.find()) {
                String namespace = matcher.group(1);
                String type = matcher.group(2);
                String path = matcher.group(3);
                String newPath = "data/" + namespace + "/data/" + type + "/" + path;
                writeEntry(newZip, entry, oldPack, newPath);
                return true;
            }
            return false;
        }

        private void writeEntry(ZipOutputStream newZip, ZipEntry entry, ZipFile oldPack, String newPath) throws IOException {
            newZip.putNextEntry(new ZipEntry(newPath));

            try (InputStream is = oldPack.getInputStream(entry)) {
                byte[] buffer = new byte[1024];
                int len;
                while ((len = is.read(buffer)) > 0) {
                    newZip.write(buffer, 0, len);
                }
            }
            newZip.closeEntry();
        }

        private void addMeta(ZipOutputStream newZip) throws IOException {
            ZipEntry entry = new ZipEntry("gunpack.meta.json");

            newZip.putNextEntry(entry);
            PackMeta meta = new PackMeta(namespace, null);
            newZip.write(GSON.toJson(meta).getBytes());
            newZip.closeEntry();
        }

        public void convert() throws FileAlreadyExistsException {
            Path newPath = FMLPaths.GAMEDIR.get().resolve("tacz");
            String newName = file.getName().replace(".zip", "") + "_converted.zip";
            File newFile = new File(newPath.toFile(), newName);

            if (newFile.isFile() && newFile.exists()) {
                throw new FileAlreadyExistsException("File already exists: " + newFile.getName());
            }

            try (ZipFile oldPack = new ZipFile(file);
                 FileOutputStream outputStream = new FileOutputStream(newFile);
                 ZipOutputStream newZip = new ZipOutputStream(outputStream)) {

                addMeta(newZip);

                var iteration = oldPack.entries();
                while (iteration.hasMoreElements()) {
                    ZipEntry entry = iteration.nextElement();
                    if (parseDisplay(newZip, entry, oldPack)) continue;
                    if (parseIndex(newZip, entry, oldPack)) continue;
                    if (parseData(newZip, entry, oldPack)) continue;
                    if (parseModels(newZip, entry, oldPack)) continue;
                    if (parseLang(newZip, entry, oldPack)) continue;
                    if (parseAnimation(newZip, entry, oldPack)) continue;
                    if (parseTexture(newZip, entry, oldPack)) continue;
                    if (parseSound(newZip, entry, oldPack)) continue;
                    if (parsePlayerAnimator(newZip, entry, oldPack)) continue;
                    if (parseTags(newZip, entry, oldPack)) continue;
                    if (parseRecipe(newZip, entry, oldPack)) continue;
                    if (parsePackInfo(newZip, entry, oldPack)) continue;
                }
            } catch (IOException e) {
                GunMod.LOGGER.warn("Failed to convert pack: {}", file.getName());
            }
        }
    }
}
