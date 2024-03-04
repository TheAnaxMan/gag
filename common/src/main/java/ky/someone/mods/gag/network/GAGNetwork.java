package ky.someone.mods.gag.network;

import dev.architectury.networking.simple.MessageType;
import dev.architectury.networking.simple.SimpleNetworkManager;
import ky.someone.mods.gag.GAGUtil;

public class GAGNetwork {
	public static final SimpleNetworkManager CHANNEL = SimpleNetworkManager.create(GAGUtil.MOD_ID);

	public static final MessageType LABELER_TRY_RENAME = CHANNEL.registerC2S("rename_item", LabelerTryRenamePacket::new);

	public static void init() {
	}
}
