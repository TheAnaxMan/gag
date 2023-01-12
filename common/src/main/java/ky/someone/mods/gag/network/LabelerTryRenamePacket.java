package ky.someone.mods.gag.network;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.MessageType;
import ky.someone.mods.gag.menu.LabelingMenu;
import net.minecraft.SharedConstants;
import net.minecraft.network.FriendlyByteBuf;

public class LabelerTryRenamePacket extends BaseC2SMessage {

	public final String name;

	public LabelerTryRenamePacket(FriendlyByteBuf buf) {
		name = buf.readUtf();
	}

	public LabelerTryRenamePacket(String name) {
		this.name = name;
	}

	@Override
	public MessageType getType() {
		return GAGNetwork.LABELER_TRY_RENAME;
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeUtf(name);
	}

	@Override
	public void handle(NetworkManager.PacketContext context) {
		context.queue(() -> {
			if (context.getPlayer().containerMenu instanceof LabelingMenu menu) {
				var name = SharedConstants.filterText(this.name);
				if (name.length() <= 50) {
					menu.setName(name);
				}
			}
		});
	}
}
