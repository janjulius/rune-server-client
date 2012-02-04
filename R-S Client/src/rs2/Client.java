package rs2;
import java.applet.AppletContext;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;

import rs2.cache.JagexArchive;
import rs2.cache.ResourceCache;
import rs2.config.Censor;
import rs2.config.Floor;
import rs2.config.IdentityKit;
import rs2.config.ItemDef;
import rs2.config.NPCDef;
import rs2.config.ObjectDef;
import rs2.config.Sequence;
import rs2.config.SpotAnim;
import rs2.config.VarBit;
import rs2.config.Varp;
import rs2.constants.Constants;
import rs2.constants.PacketConstants;
import rs2.constants.SizeConstants;
import rs2.constants.SkillConstants;
import rs2.cryption.MD5;
import rs2.graphics.IndexedImage;
import rs2.graphics.RSDrawingArea;
import rs2.graphics.RSFont;
import rs2.graphics.RSImage;
import rs2.graphics.RSImageProducer;
import rs2.graphics.RSInterface;
import rs2.graphics.Rasterizer;
import rs2.resource.Resource;
import rs2.resource.ResourceProvider;
import rs2.sign.signlink;
import rs2.util.TextUtils;
import rs2.world.GroundDecoration;
import rs2.world.InteractableObject;
import rs2.world.ObjectOnTile;
import rs2.world.Projectile;
import rs2.world.StillGraphics;
import rs2.world.WallDecoration;
import rs2.world.WallObject;
import rs2.world.render.MapRegion;
import rs2.world.render.SceneGraph;
import rs2.world.tile.TileSetting;

public final class Client extends RSApplet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7064771078339161069L;

	public static int mapX, mapY;

	public void addObject(int objectId, int x, int y, int z, int face, int type) {
		int mX = mapX - 6;
		int mY = mapY - 6;
		int x2 = x - (mX * 8);
		int y2 = y - (mY * 8);
		int i15 = 40 >> 2;
		int l17 = anIntArray1177[i15];
		if (y2 > 0 && y2 < 103 && x2 > 0 && x2 < 103) {
			method130(-1, objectId, face, l17, y2, type, z, x2, 0);
		}
	}

	private static String formatAmount(int amount) {
		String string = String.valueOf(amount);
		for(int index = string.length() - 3; index > 0; index -= 3) {
			string = string.substring(0, index) + "," + string.substring(index);
		}
		if(string.length() > 8) {
			string = "@gre@" + string.substring(0, string.length() - 8) + " million @whi@(" + string + ")";
		} else {
			if(string.length() > 4) {
				string = "@cya@" + string.substring(0, string.length() - 4) + "K @whi@(" + string + ")";
			}
		}
		return " " + string;
	}

	private void stopMidi() {
		signlink.midifade = 0;
		signlink.midi = "stop";
	}

	private void connect() {
		int j = 5;
		expectedCRCs[8] = 0;
		int k = 0;
		while(expectedCRCs[8] == 0) {
			String s = "Unknown problem";
			displayProgress("Connecting to web server", 20);
			try {
				DataInputStream datainputstream = openJagGrabInputStream("crc" + (int)(Math.random() * 99999999D) + "-" + 317);
				ByteBuffer buffer = new ByteBuffer(new byte[40]);
				datainputstream.readFully(buffer.buffer, 0, 40);
				datainputstream.close();
				for(int index = 0; index < 9; index++) {
					expectedCRCs[index] = buffer.getInt();
				}
				int result = buffer.getInt();
				int crc = 1234;
				for(int index = 0; index < 9; index++) {
					crc = (crc << 1) + expectedCRCs[index];
				}
				if(result != crc) {
					s = "checksum problem";
					expectedCRCs[8] = 0;
				}
			} catch(EOFException _ex) {
				s = "EOF problem";
				expectedCRCs[8] = 0;
			} catch(IOException _ex) {
				s = "connection problem";
				expectedCRCs[8] = 0;
			} catch(Exception _ex) {
				s = "logic problem";
				expectedCRCs[8] = 0;
				if(!signlink.reporterror)
					return;
			}
			if(expectedCRCs[8] == 0) {
				k++;
				for(int l = j; l > 0; l--) {
					if(k >= 10) {
						displayProgress("Game updated - please reload page", 10);
						l = 10;
					} else {
						displayProgress(s + " - Will retry in " + l + " secs.", 10);
					}
					try {
						Thread.sleep(1000L);
					} catch(Exception _ex) {
					}
				}
				j *= 2;
				if(j > 60) {
					j = 60;
				}
				aBoolean872 = !aBoolean872;
			}
		}
	}

	private boolean menuHasAddFriend(int j) {
		if(j < 0) {
			return false;
		}
		int k = menuActionID[j];
		if(k >= 2000) {
			k -= 2000;
		}
		return k == 337;
	}

	/**
	 * Returns the number of lines the chatbox has.
	 * @return
	 */
	public int getLines() {
		return isFixed() ? 5 : 8;
	}

	public String getPrefix(int rights) {
		switch (rights) {
			case 1:
				return "@cr1@";
			case 2:
				return "@cr2@";
			case 3:
				return "@cr3@";
			case 4:
				return "@com@";
			case 5:
				return "@des@";
			case 6:
				return "@vet@";
			case 7:
				return "@don@";
				
			default:
				return "";
		}
	}

	public int getPrefixRights(String prefix) {
		if (prefix.equals("@cr1@")) {
			return 1;
		} else if (prefix.equals("@cr2@")) {
			return 2;
		} else if (prefix.equals("@cr3@")) {
			return 3;
		} else if (prefix.equals("@dev@")) {
			return 4;
		} else if (prefix.equals("@gfx@")) {
			return 5;
		} else if (prefix.equals("@vet@")) {
			return 6;
		} else if (prefix.equals("@don@")) {
			return 7;
		}
		return 0;
	}

	public int frameVersion = 317;

	public int getFrameVersion() {
		if (!isFixed() && frameVersion < 508) {
			return 508;
		}
		return frameVersion;
	}

	public void setFrameVersion(int version) {
		frameVersion = version;
	}

	public void drawChannelButtons(int x, int y) {
		String[] channels = { "All", "Game", "Public", "Private", "Clan", "Yell", "Trade" };
		String[] modes = { "On", "Friends", "Off", "Hide" };
		int[] colors = { 0x00ff00, 0xffff00, 0xff0000, 0x00ffff };
		int[] channelModes = { -1, -1, publicChatMode, privateChatMode, clanChatMode, yellChatMode, tradeMode };
		int[] offsetY = { 4, 4, -1, -1, -1, -1, -1 };
		RSImage channel = new RSImage("channel");
		RSImage clicked = new RSImage("channel_clicked");
		RSImage hovered = new RSImage("channel_hover");
		RSImage clicked_hovered = new RSImage("channel_clicked_hover");
		RSImage report = new RSImage("report");
		RSImage report_hover = new RSImage("report_hover");
		int posx = x + 5;
		y += 143;
		for (int index = 0; index < channels.length; index++, posx += channel.myWidth + 1) {
			if (getCurrentChatMode() == index) {
				if (chatModeHover == index) {
					clicked_hovered.drawImage(posx, y);
				} else {
					clicked.drawImage(posx, y);
				}
			} else if (chatModeHover == index) {
				hovered.drawImage(posx, y);
			} else {
				channel.drawImage(posx, y);
			}
			small.drawCenteredString(channels[index], posx + (channel.myWidth / 2), y + (channel.myHeight / 2) + offsetY[index], 0xFFFFFF, true);
			if (channelModes[index] != -1) {
				small.drawCenteredString(modes[channelModes[index]], posx + (channel.myWidth / 2), y + (channel.myHeight / 2) + offsetY[index] + 11, colors[channelModes[index]], true);
			}
		}
		if (chatModeHover ==  7) {
			report_hover.drawImage(posx, y);
		} else {
			report.drawImage(posx, y);
		}
		small.drawCenteredString("Report Abuse", posx + (report.myWidth / 2), y + (report.myHeight / 2) + 4, 0xFFFFFF, true);
	}

	public boolean showChatArea = true;

	private void drawChatArea() {
		int offsetX = 0;
		int offsetY = isFixed() ? 0 : getClientHeight() - 165;
		int textOffsetY = offsetY + (isFixed() ? 0 : 7);
		if (isFixed()) {
			chatArea.initDrawingArea();
		}
		Rasterizer.lineOffsets = chatAreaTextureArray;
		RSImage chat = new RSImage("chatback");
		RSImage background = new RSImage("chatbackground");
		drawChannelButtons(offsetX, offsetY);
		if (!showChatArea) {
			return;
		}
		if (!isFixed()) {
			if (promptRaised || dialogState == 1 || dialogState == 2 || aString844 != null || backDialogID != -1 || dialogID != -1) {
				background.drawImage(offsetX, offsetY);
			} else {
				chat.drawARGBImage(7, textOffsetY);
			}
		} else {
			chatBack.drawImage(0, 0);
		}
		if(promptRaised) {
			bold.drawCenteredString(promptMessage, offsetX + (isFixed() ? (chatBack.myWidth / 2) : (background.myWidth / 2)), offsetY + (isFixed() ? 40 : 60), 0, false);
			bold.drawCenteredString(promptInput + "*", offsetX + (isFixed() ? (chatBack.myWidth / 2) : (background.myWidth / 2)), offsetY + (isFixed() ? 60 : 80), 128, false);
		} else if(dialogState == 1) {
			bold.drawCenteredString("Enter amount:", offsetX + (isFixed() ? (chatBack.myWidth / 2) : (background.myWidth / 2)), offsetY + (isFixed() ? 40 : 60), 0, false);
			bold.drawCenteredString(amountOrNameInput + "*", offsetX + (isFixed() ? (chatBack.myWidth / 2) : (background.myWidth / 2)), offsetY + (isFixed() ? 60 : 80), 128, false);
		} else if(dialogState == 2) {
			bold.drawCenteredString("Enter name:", offsetX + (isFixed() ? (chatBack.myWidth / 2) : (background.myWidth / 2)), offsetY + (isFixed() ? 40 : 60), 0, false);
			bold.drawCenteredString(amountOrNameInput + "*", offsetX + (isFixed() ? (chatBack.myWidth / 2) : (background.myWidth / 2)), offsetY + (isFixed() ? 60 : 80), 128, false);
		} else if(aString844 != null) {
			bold.drawCenteredString(aString844, offsetX + (isFixed() ? (chatBack.myWidth / 2) : (background.myWidth / 2)), offsetY + (isFixed() ? 40 : 60), 0, false);
			bold.drawCenteredString("Click to continue", offsetX + (isFixed() ? (chatBack.myWidth / 2) : (background.myWidth / 2)), offsetY + (isFixed() ? 60 : 80), 128, false);
		} else if(backDialogID != -1) {
			drawInterface(RSInterface.cache[backDialogID], offsetX, 0, offsetY);
		} else if(dialogID != -1) {
			drawInterface(RSInterface.cache[dialogID], offsetX, 0, offsetY);
		} else {
			RSFont font = regular;
			int offset = 0;
			int x = getLines() == 5 ? 4 : 9;
			RSDrawingArea.setBounds(0 + offsetX, (getLines() == 5 ? 463 : 494) + offsetX, (isFixed() ? 0 : 1) + textOffsetY, (getLines() == 5 ? 77 : 115) + textOffsetY);
			for(int index = 0; index < 100; index++)
				if(chatMessages[index] != null) {
					int type = chatTypes[index];
					int y = ((isFixed() ? 70 : 110) - offset * 14) + anInt1089;
					String name = chatNames[index];
					String prefix = name;
					int rights = 0;
					if (name != null && name.indexOf("@") == 0) {
						name = name.substring(5);
						rights = getPrefixRights(prefix.substring(0, prefix.indexOf(name)));
					}
					if((getCurrentChatMode() == ALL || getCurrentChatMode() == GAME) && type == 0) {
						if(y > 0 && y < 210) {
							font.drawShadowedString(chatMessages[index], x, y + textOffsetY, isFixed() ? 0 : 0xFFFFFF, !isFixed());
						}
						offset++;
					}
					if((getCurrentChatMode() == ALL || getCurrentChatMode() == PUBLIC) && (type == 1 || type == 2) && (type == 1 || publicChatMode == 0 || publicChatMode == 1 && isFriendOrSelf(name))) {
						if(y > 0 && y < 210) {
							int xpos = x;
							if (rights != 0) {
								modIcons[rights - 1].drawImage(xpos, y - 12 + textOffsetY);
								xpos += 14;
							}
							font.drawShadowedString(name + ":", xpos, y + textOffsetY, isFixed() ? 0 : 0xFFFFFF, !isFixed());
							xpos += font.getEffectTextWidth(name) + 8;
							font.drawShadowedString(chatMessages[index], xpos, y + textOffsetY, isFixed() ? 255 : 0x7FA9FF, !isFixed());
						}
						offset++;
					}
					if((getCurrentChatMode() == ALL || getCurrentChatMode() == PRIVATE) && (type == 3 || type == 7) && splitPrivateChat == 0 && (type == 7 || privateChatMode == 0 || privateChatMode == 1 && isFriendOrSelf(name))) {
						if(y > 0 && y < 210) {
							int xpos = x;
							font.drawShadowedString("From", xpos, y + textOffsetY, isFixed() ? 0 : 0xFFFFFF, !isFixed());
							xpos += font.getEffectTextWidth("From ");
							if (rights != 0) {
								modIcons[rights - 1].drawImage(xpos, y - 12 + textOffsetY);
								xpos += 14;
							}
							font.drawShadowedString(name + ":", xpos, y + textOffsetY, isFixed() ? 0 : 0xFFFFFF, !isFixed());
							xpos += font.getEffectTextWidth(name) + 8;
							font.drawShadowedString(chatMessages[index], xpos, y + textOffsetY, isFixed() ? 0x800000 : 0xFF5256, !isFixed());
						}
						offset++;
					}
					if((getCurrentChatMode() == ALL || getCurrentChatMode() == TRADE) && type == 4 && (tradeMode == 0 || tradeMode == 1 && isFriendOrSelf(name))) {
						if(y > 0 && y < 210) {
							font.drawShadowedString(name + " " + chatMessages[index], x, y + textOffsetY, isFixed() ? 0x800080 : 0xFF00D4, !isFixed());
						}
						offset++;
					}
					if((getCurrentChatMode() == ALL || getCurrentChatMode() == PRIVATE) && type == 5 && splitPrivateChat == 0 && privateChatMode < 2) {
						if(y > 0 && y < 210) {
							font.drawShadowedString(chatMessages[index], x, y + textOffsetY, isFixed() ? 0x800000 : 0xFF5256, !isFixed());
						}
						offset++;
					}
					if((getCurrentChatMode() == ALL || getCurrentChatMode() == PRIVATE) && type == 6 && splitPrivateChat == 0 && privateChatMode < 2) {
						if(y > 0 && y < 210) {
							font.drawShadowedString("To " + name + ":", x, y + textOffsetY, isFixed() ? 0 : 0xFFFFFF, !isFixed());
							font.drawShadowedString(chatMessages[index], (x + 8) + font.getEffectTextWidth("To " + name), y + textOffsetY, isFixed() ? 0x800000 : 0xFF5256, !isFixed());
						}
						offset++;
					}
					if((getCurrentChatMode() == ALL || getCurrentChatMode() == TRADE) && type == 8 && (tradeMode == 0 || tradeMode == 1 && isFriendOrSelf(name))) {
						if(y > 0 && y < 210) {
							font.drawBasicString(name + " " + chatMessages[index], x, y + textOffsetY, 0x7e3200);
						}
						offset++;
					}
				}

			RSDrawingArea.setDefaultArea();
			anInt1211 = offset * 14 + 7;
			if(anInt1211 < (getLines() == 5 ? 78 : 111)) {
				anInt1211 = (getLines() == 5 ? 78 : 111);
			}
			drawScrollbar((getLines() == 5 ? 77 : 114), anInt1211 - anInt1089 - (getLines() == 5 ? 77 : 113), 0 + textOffsetY, (getLines() == 5 ? 463 : 496), anInt1211);
			String name;
			if(myPlayer != null && myPlayer.name != null) {
				name = myPlayer.name;
			} else {
				name = getUsername();
			}
			font.drawShadowedString(name + ":", x, (getLines() == 5 ? 90 : 126) + textOffsetY, isFixed() ? 0 : 0xFFFFFF, !isFixed());
			font.drawShadowedString(inputString + "*", x + font.getEffectTextWidth(name + ": "), (getLines() == 5 ? 90 : 126) + textOffsetY, isFixed() ? 255 : 0x7FA9FF, !isFixed());
			if (isFixed()) {
				RSDrawingArea.drawHorizontalLine(0, 77, 479, 0);
			}
		}
		if(menuOpen && menuScreenArea == 2) {
			drawMenu();
		}
		if (isFixed()) {
			chatArea.drawGraphics(17, 357, super.graphics);
		}
		gameArea.initDrawingArea();
		Rasterizer.lineOffsets = gameAreaTextureArray;
	}

	public void init() {
		nodeID = Integer.parseInt(getParameter("nodeid"));
		portOff = Integer.parseInt(getParameter("portoff"));
		String s = getParameter("lowmem");
		if(s != null && s.equals("1")) {
			setLowMem();
		} else {
			setHighMem();
		}
		String s1 = getParameter("free");
		isMembers = !(s1 != null && s1.equals("1"));
		instance = this;
		initClientFrame(765, 503);
	}

	public void startRunnable(Runnable runnable, int i)
	{
		if(i > 10)
			i = 10;
		if(signlink.mainapp != null)
		{
			signlink.startthread(runnable, i);
		} else
		{
			super.startRunnable(runnable, i);
		}
	}

	public Socket openSocket(int i)
			throws IOException
			{
		if(signlink.mainapp != null)
			return signlink.opensocket(i);
		else
			return new Socket(InetAddress.getByName(getCodeBase().getHost()), i);
			}

	private void processMenuClick()
	{
		if(activeInterfaceType != 0)
			return;
		int j = super.clickMode3;
		if(spellSelected == 1 && super.saveClickX >= 516 && super.saveClickY >= 160 && super.saveClickX <= 765 && super.saveClickY <= 205)
			j = 0;
		if(menuOpen)
		{
			if(j != 1)
			{
				int k = super.mouseX;
				int j1 = super.mouseY;
				if(menuScreenArea == 0)
				{
					k -= 4;
					j1 -= 4;
				}
				if(menuScreenArea == 1)
				{
					k -= 553;
					j1 -= 205;
				}
				if(menuScreenArea == 2)
				{
					k -= 17;
					j1 -= 357;
				}
				if(k < menuOffsetX - 10 || k > menuOffsetX + menuWidth + 10 || j1 < menuOffsetY - 10 || j1 > menuOffsetY + menuHeight + 10)
				{
					menuOpen = false;
					if(menuScreenArea == 1)
						redrawTabArea = true;
					if(menuScreenArea == 2)
						inputTaken = true;
				}
			}
			if(j == 1)
			{
				int l = menuOffsetX;
				int k1 = menuOffsetY;
				int i2 = menuWidth;
				int k2 = super.saveClickX;
				int l2 = super.saveClickY;
				if(menuScreenArea == 0)
				{
					k2 -= 4;
					l2 -= 4;
				}
				if(menuScreenArea == 1)
				{
					k2 -= 553;
					l2 -= 205;
				}
				if(menuScreenArea == 2)
				{
					k2 -= 17;
					l2 -= 357;
				}
				int i3 = -1;
				for(int j3 = 0; j3 < menuActionRow; j3++)
				{
					int k3 = k1 + 31 + (menuActionRow - 1 - j3) * 15;
					if(k2 > l && k2 < l + i2 && l2 > k3 - 13 && l2 < k3 + 3)
						i3 = j3;
				}

				if(i3 != -1)
					doAction(i3);
				menuOpen = false;
				if(menuScreenArea == 1)
					redrawTabArea = true;
				if(menuScreenArea == 2)
				{
					inputTaken = true;
				}
			}
		} else
		{
			if(j == 1 && menuActionRow > 0)
			{
				int i1 = menuActionID[menuActionRow - 1];
				if(i1 == 632 || i1 == 78 || i1 == 867 || i1 == 431 || i1 == 53 || i1 == 74 || i1 == 454 || i1 == 539 || i1 == 493 || i1 == 847 || i1 == 447 || i1 == 1125)
				{
					int l1 = menuActionCmd2[menuActionRow - 1];
					int j2 = menuActionCmd3[menuActionRow - 1];
					RSInterface class9 = RSInterface.cache[j2];
					if(class9.itemsSwappable || class9.deletesTargetSlot)
					{
						aBoolean1242 = false;
						anInt989 = 0;
						anInt1084 = j2;
						anInt1085 = l1;
						activeInterfaceType = 2;
						anInt1087 = super.saveClickX;
						anInt1088 = super.saveClickY;
						if(RSInterface.cache[j2].parentId == openInterfaceID)
							activeInterfaceType = 1;
						if(RSInterface.cache[j2].parentId == backDialogID)
							activeInterfaceType = 3;
						return;
					}
				}
			}
			if(j == 1 && (anInt1253 == 1 || menuHasAddFriend(menuActionRow - 1)) && menuActionRow > 2)
				j = 2;
			if(j == 1 && menuActionRow > 0)
				doAction(menuActionRow - 1);
			if(j == 2 && menuActionRow > 0)
				determineMenuSize();
		}
	}

	private void saveMidi(boolean flag, byte abyte0[])
	{
		signlink.midifade = flag ? 1 : 0;
		signlink.midisave(abyte0, abyte0.length);
	}

	private void method22()
	{
		try
		{
			anInt985 = -1;
			aClass19_1056.removeAll();
			aClass19_1013.removeAll();
			Rasterizer.clearTextureCache();
			unlinkMRUNodes();
			worldController.initToNull();
			System.gc();
			for(int i = 0; i < 4; i++)
				aClass11Array1230[i].init();

			for(int l = 0; l < 4; l++)
			{
				for(int k1 = 0; k1 < 104; k1++)
				{
					for(int j2 = 0; j2 < 104; j2++)
						byteGroundArray[l][k1][j2] = 0;

				}

			}

			MapRegion sceneGraph = new MapRegion(byteGroundArray, intGroundArray);
			int k2 = aByteArrayArray1183.length;
			out.putOpCode(0);
			if(!aBoolean1159)
			{
				for(int i3 = 0; i3 < k2; i3++)
				{
					int i4 = (anIntArray1234[i3] >> 8) * 64 - baseX;
					int k5 = (anIntArray1234[i3] & 0xff) * 64 - baseY;
					byte abyte0[] = aByteArrayArray1183[i3];
					if(abyte0 != null)
						sceneGraph.loadTerrain(abyte0, k5, i4, (anInt1069 - 6) * 8, (anInt1070 - 6) * 8, aClass11Array1230);
				}

				for(int j4 = 0; j4 < k2; j4++)
				{
					int l5 = (anIntArray1234[j4] >> 8) * 64 - baseX;
					int k7 = (anIntArray1234[j4] & 0xff) * 64 - baseY;
					byte abyte2[] = aByteArrayArray1183[j4];
					if(abyte2 == null && anInt1070 < 800)
						sceneGraph.initMapTables(k7, 64, 64, l5);
				}

				anInt1097++;
				if(anInt1097 > 160)
				{
					anInt1097 = 0;
					out.putOpCode(238);
					out.writeWordBigEndian(96);
				}
				out.putOpCode(0);
				for(int i6 = 0; i6 < k2; i6++)
				{
					byte abyte1[] = aByteArrayArray1247[i6];
					if(abyte1 != null)
					{
						int l8 = (anIntArray1234[i6] >> 8) * 64 - baseX;
						int k9 = (anIntArray1234[i6] & 0xff) * 64 - baseY;
						sceneGraph.loadObjects(l8, aClass11Array1230, k9, worldController, abyte1);
					}
				}

			}
			if(aBoolean1159)
			{
				for(int j3 = 0; j3 < 4; j3++)
				{
					for(int k4 = 0; k4 < 13; k4++)
					{
						for(int j6 = 0; j6 < 13; j6++)
						{
							int l7 = anIntArrayArrayArray1129[j3][k4][j6];
							if(l7 != -1)
							{
								int i9 = l7 >> 24 & 3;
						int l9 = l7 >> 1 & 3;
				int j10 = l7 >> 14 & 0x3ff;
				int l10 = l7 >> 3 & 0x7ff;
				int j11 = (j10 / 8 << 8) + l10 / 8;
				for(int l11 = 0; l11 < anIntArray1234.length; l11++)
				{
					if(anIntArray1234[l11] != j11 || aByteArrayArray1183[l11] == null)
						continue;
					sceneGraph.loadMapChunk(i9, l9, aClass11Array1230, k4 * 8, (j10 & 7) * 8, aByteArrayArray1183[l11], (l10 & 7) * 8, j3, j6 * 8);
					break;
				}

							}
						}

					}

				}

				for(int l4 = 0; l4 < 13; l4++)
				{
					for(int k6 = 0; k6 < 13; k6++)
					{
						int i8 = anIntArrayArrayArray1129[0][l4][k6];
						if(i8 == -1)
							sceneGraph.initMapTables(k6 * 8, 8, 8, l4 * 8);
					}

				}

				out.putOpCode(0);
				for(int l6 = 0; l6 < 4; l6++)
				{
					for(int j8 = 0; j8 < 13; j8++)
					{
						for(int j9 = 0; j9 < 13; j9++)
						{
							int i10 = anIntArrayArrayArray1129[l6][j8][j9];
							if(i10 != -1)
							{
								int k10 = i10 >> 24 & 3;
						int i11 = i10 >> 1 & 3;
				int k11 = i10 >> 14 & 0x3ff;
				int i12 = i10 >> 3 & 0x7ff;
				int j12 = (k11 / 8 << 8) + i12 / 8;
				for(int k12 = 0; k12 < anIntArray1234.length; k12++)
				{
					if(anIntArray1234[k12] != j12 || aByteArrayArray1247[k12] == null)
						continue;
					sceneGraph.method183(aClass11Array1230, worldController, k10, j8 * 8, (i12 & 7) * 8, l6, aByteArrayArray1247[k12], (k11 & 7) * 8, i11, j9 * 8);
					break;
				}

							}
						}

					}

				}

			}
			out.putOpCode(0);
			sceneGraph.addTiles(aClass11Array1230, worldController);
			gameArea.initDrawingArea();
			out.putOpCode(0);
			int k3 = MapRegion.setZ;
			if(k3 > floor_level)
				k3 = floor_level;
			if(k3 < floor_level - 1)
				k3 = floor_level - 1;
			if(lowMem)
				worldController.setHeightLevel(MapRegion.setZ);
			else
				worldController.setHeightLevel(0);
			for(int i5 = 0; i5 < 104; i5++)
			{
				for(int i7 = 0; i7 < 104; i7++)
					spawnGroundItem(i5, i7);

			}

			anInt1051++;
			if(anInt1051 > 98)
			{
				anInt1051 = 0;
				out.putOpCode(150);
			}
			method63();
		}
		catch(Exception exception) { }
		ObjectDef.mruNodes1.unlinkAll();
		if(super.mainFrame != null)
		{
			out.putOpCode(210);
			out.putInt(0x3f008edd);
		}
		if(lowMem && signlink.cache_dat != null)
		{
			int j = onDemandFetcher.getVersionCount(0);
			for(int i1 = 0; i1 < j; i1++)
			{
				int l1 = onDemandFetcher.getModelIndex(i1);
				if((l1 & 0x79) == 0)
					Model.method461(i1);
			}

		}
		System.gc();
		Rasterizer.resetTextures();
		onDemandFetcher.method566();
		int k = (anInt1069 - 6) / 8 - 1;
		int j1 = (anInt1069 + 6) / 8 + 1;
		int i2 = (anInt1070 - 6) / 8 - 1;
		int l2 = (anInt1070 + 6) / 8 + 1;
		if(aBoolean1141)
		{
			k = 49;
			j1 = 50;
			i2 = 49;
			l2 = 50;
		}
		for(int l3 = k; l3 <= j1; l3++)
		{
			for(int j5 = i2; j5 <= l2; j5++)
				if(l3 == k || l3 == j1 || j5 == i2 || j5 == l2)
				{
					int j7 = onDemandFetcher.method562(0, j5, l3);
					if(j7 != -1)
						onDemandFetcher.method560(j7, 3);
					int k8 = onDemandFetcher.method562(1, j5, l3);
					if(k8 != -1)
						onDemandFetcher.method560(k8, 3);
				}

		}

	}

	private void unlinkMRUNodes()
	{
		ObjectDef.mruNodes1.unlinkAll();
		ObjectDef.mruNodes2.unlinkAll();
		NPCDef.mruNodes.unlinkAll();
		ItemDef.mruNodes2.unlinkAll();
		ItemDef.mruNodes1.unlinkAll();
		Player.mruNodes.unlinkAll();
		SpotAnim.aMRUNodes_415.unlinkAll();
	}

	private void method24(int i)
	{
		int ai[] = aClass30_Sub2_Sub1_Sub1_1263.myPixels;
		int j = ai.length;
		for(int k = 0; k < j; k++)
			ai[k] = 0;

		for(int l = 1; l < 103; l++)
		{
			int i1 = 24628 + (103 - l) * 512 * 4;
			for(int k1 = 1; k1 < 103; k1++)
			{
				if((byteGroundArray[i][k1][l] & 0x18) == 0)
					worldController.drawMinimapTile(ai, i1, i, k1, l);
				if(i < 3 && (byteGroundArray[i + 1][k1][l] & 8) != 0)
					worldController.drawMinimapTile(ai, i1, i + 1, k1, l);
				i1 += 4;
			}

		}

		int j1 = ((238 + (int)(Math.random() * 20D)) - 10 << 16) + ((238 + (int)(Math.random() * 20D)) - 10 << 8) + ((238 + (int)(Math.random() * 20D)) - 10);
		int l1 = (238 + (int)(Math.random() * 20D)) - 10 << 16;
		aClass30_Sub2_Sub1_Sub1_1263.method343();
		for(int i2 = 1; i2 < 103; i2++)
		{
			for(int j2 = 1; j2 < 103; j2++)
			{
				if((byteGroundArray[i][j2][i2] & 0x18) == 0)
					method50(i2, j1, j2, l1, i);
				if(i < 3 && (byteGroundArray[i + 1][j2][i2] & 8) != 0)
					method50(i2, j1, j2, l1, i + 1);
			}

		}

		gameArea.initDrawingArea();
		anInt1071 = 0;
		for(int k2 = 0; k2 < 104; k2++)
		{
			for(int l2 = 0; l2 < 104; l2++)
			{
				int i3 = worldController.getGroundDecorationUID(floor_level, k2, l2);
				if(i3 != 0)
				{
					i3 = i3 >> 14 & 0x7fff;
			int j3 = ObjectDef.getDef(i3).anInt746;
			if(j3 >= 0)
			{
				int k3 = k2;
				int l3 = l2;
				if(j3 != 22 && j3 != 29 && j3 != 34 && j3 != 36 && j3 != 46 && j3 != 47 && j3 != 48)
				{
					byte byte0 = 104;
					byte byte1 = 104;
					int ai1[][] = aClass11Array1230[floor_level].clipData;
					for(int i4 = 0; i4 < 10; i4++)
					{
						int j4 = (int)(Math.random() * 4D);
						if(j4 == 0 && k3 > 0 && k3 > k2 - 3 && (ai1[k3 - 1][l3] & 0x1280108) == 0)
							k3--;
						if(j4 == 1 && k3 < byte0 - 1 && k3 < k2 + 3 && (ai1[k3 + 1][l3] & 0x1280180) == 0)
							k3++;
						if(j4 == 2 && l3 > 0 && l3 > l2 - 3 && (ai1[k3][l3 - 1] & 0x1280102) == 0)
							l3--;
						if(j4 == 3 && l3 < byte1 - 1 && l3 < l2 + 3 && (ai1[k3][l3 + 1] & 0x1280120) == 0)
							l3++;
					}

				}
				aClass30_Sub2_Sub1_Sub1Array1140[anInt1071] = mapFunctions[j3];
				anIntArray1072[anInt1071] = k3;
				anIntArray1073[anInt1071] = l3;
				anInt1071++;
			}
				}
			}

		}

	}

	private void spawnGroundItem(int i, int j)
	{
		Deque class19 = groundArray[floor_level][i][j];
		if(class19 == null)
		{
			worldController.removeGroundItemTile(floor_level, i, j);
			return;
		}
		int k = 0xfa0a1f01;
		Object obj = null;
		for(Item item = (Item)class19.head(); item != null; item = (Item)class19.next())
		{
			ItemDef itemDef = ItemDef.getDef(item.ID);
			int l = itemDef.value;
			if(itemDef.stackable)
				l *= item.anInt1559 + 1;
			//	notifyItemSpawn(item, i + baseX, j + baseY);

			if(l > k)
			{
				k = l;
				obj = item;
			}
		}

		class19.insertTail(((Node) (obj)));
		Object obj1 = null;
		Object obj2 = null;
		for(Item class30_sub2_sub4_sub2_1 = (Item)class19.head(); class30_sub2_sub4_sub2_1 != null; class30_sub2_sub4_sub2_1 = (Item)class19.next())
		{
			if(class30_sub2_sub4_sub2_1.ID != ((Item) (obj)).ID && obj1 == null)
				obj1 = class30_sub2_sub4_sub2_1;
			if(class30_sub2_sub4_sub2_1.ID != ((Item) (obj)).ID && class30_sub2_sub4_sub2_1.ID != ((Item) (obj1)).ID && obj2 == null)
				obj2 = class30_sub2_sub4_sub2_1;
		}

		int i1 = i + (j << 7) + 0x60000000;
		worldController.addGroundItemTile(i, i1, ((Animable) (obj1)), method42(floor_level, j * 128 + 64, i * 128 + 64), ((Animable) (obj2)), ((Animable) (obj)), floor_level, j);
	}

	private void method26(boolean flag)
	{
		for(int j = 0; j < npcCount; j++)
		{
			NPC npc = npcArray[npcIndices[j]];
			int k = 0x20000000 + (npcIndices[j] << 14);
			if(npc == null || !npc.isVisible() || npc.desc.aBoolean93 != flag)
				continue;
			int l = npc.currentX >> 7;
			int i1 = npc.currentY >> 7;
		if(l < 0 || l >= 104 || i1 < 0 || i1 >= 104)
			continue;
		if(npc.boundDim == 1 && (npc.currentX & 0x7f) == 64 && (npc.currentY & 0x7f) == 64)
		{
			if(anIntArrayArray929[l][i1] == anInt1265)
				continue;
			anIntArrayArray929[l][i1] = anInt1265;
		}
		if(!npc.desc.aBoolean84)
			k += 0x80000000;
		worldController.method285(floor_level, npc.currentRotation, method42(floor_level, npc.currentY, npc.currentX), k, npc.currentY, (npc.boundDim - 1) * 64 + 60, npc.currentX, npc, npc.aBoolean1541);
		}
	}

	private boolean replayWave()
	{
		return signlink.wavereplay();
	}

	private void loadError()
	{
		String s = "ondemand";//was a constant parameter
		System.out.println(s);
		try
		{
			getAppletContext().showDocument(new URL(getCodeBase(), "loaderror_" + s + ".html"));
		}
		catch(Exception exception)
		{
			exception.printStackTrace();
		}
		do
			try
		{
				Thread.sleep(1000L);
		}
		catch(Exception _ex) { }
		while(true);
	}

	private void buildInterfaceMenu(RSInterface rsi, int x, int y, int mouse_x, int mouse_y, int offsetY) {
		if(rsi.type != 0 || rsi.children == null || rsi.showInterface) {
			return;
		}
		if(mouse_x < x || mouse_y < y || mouse_x > x + rsi.width || mouse_y > y + rsi.height) {
			return;
		}
		int totalChildren = rsi.children.length;
		for(int index = 0; index < totalChildren; index++) {
			int xPos = rsi.childX[index] + x;
			int yPos = (rsi.childY[index] + y) - offsetY;
			RSInterface child = RSInterface.cache[rsi.children[index]];
			xPos += child.drawOffsetX;
			yPos += child.drawOffsetY;
			if((child.hoverId >= 0 || child.disabledHoverColor != 0) && mouse_x >= xPos && mouse_y >= yPos && mouse_x < xPos + child.width && mouse_y < yPos + child.height) {
				if(child.hoverId >= 0) {
					anInt886 = child.hoverId;
				} else {
					anInt886 = child.id;
				}
			}
			if (child.type == 8 && mouse_x >= xPos && mouse_y >= yPos && mouse_x < xPos + child.width && mouse_y < yPos + child.height) {
                anInt1315 = child.id;
            }
			if(child.type == 0) {
				buildInterfaceMenu(child, xPos, yPos, mouse_x, mouse_y, child.scrollPosition);
				if(child.scrollMax > child.height)
					method65(xPos + child.width, child.height, mouse_x, mouse_y, child, yPos, true, child.scrollMax);
			} else {
				if(child.actionType == 1 && mouse_x >= xPos && mouse_y >= yPos && mouse_x < xPos + child.width && mouse_y < yPos + child.height)
				{
					boolean flag = false;
					if(child.contentType != 0)
						flag = buildFriendsListMenu(child);
					if(!flag)
					{
						//System.out.println("1"+class9_1.tooltip + ", " + class9_1.interfaceID);
						menuActionName[menuActionRow] = child.tooltip + ", " + child.id;
						menuActionID[menuActionRow] = 315;
						menuActionCmd3[menuActionRow] = child.id;
						menuActionRow++;
					}
				}
				if(child.actionType == 2 && spellSelected == 0 && mouse_x >= xPos && mouse_y >= yPos && mouse_x < xPos + child.width && mouse_y < yPos + child.height)
				{
					String s = child.selectedActionName;
					if(s.indexOf(" ") != -1)
						s = s.substring(0, s.indexOf(" "));
					menuActionName[menuActionRow] = s + " @gre@" + child.spellName;
					menuActionID[menuActionRow] = 626;
					menuActionCmd3[menuActionRow] = child.id;
					menuActionRow++;
				}
				if(child.actionType == 3 && mouse_x >= xPos && mouse_y >= yPos && mouse_x < xPos + child.width && mouse_y < yPos + child.height)
				{
					menuActionName[menuActionRow] = "Close";
					menuActionID[menuActionRow] = 200;
					menuActionCmd3[menuActionRow] = child.id;
					menuActionRow++;
				}
				if(child.actionType == 4 && mouse_x >= xPos && mouse_y >= yPos && mouse_x < xPos + child.width && mouse_y < yPos + child.height)
				{
					//System.out.println("2"+class9_1.tooltip + ", " + class9_1.interfaceID);
					menuActionName[menuActionRow] = child.tooltip + ", " + child.id;
					menuActionID[menuActionRow] = 169;
					menuActionCmd3[menuActionRow] = child.id;
					menuActionRow++;
				}
				if(child.actionType == 5 && mouse_x >= xPos && mouse_y >= yPos && mouse_x < xPos + child.width && mouse_y < yPos + child.height)
				{
					//System.out.println("3"+class9_1.tooltip + ", " + class9_1.interfaceID);
					menuActionName[menuActionRow] = child.tooltip + ", " + child.id;
					menuActionID[menuActionRow] = 646;
					menuActionCmd3[menuActionRow] = child.id;
					menuActionRow++;
				}
				if(child.actionType == 6 && !aBoolean1149 && mouse_x >= xPos && mouse_y >= yPos && mouse_x < xPos + child.width && mouse_y < yPos + child.height)
				{
					//System.out.println("4"+class9_1.tooltip + ", " + class9_1.interfaceID);
					menuActionName[menuActionRow] = child.tooltip + ", " + child.id;
					menuActionID[menuActionRow] = 679;
					menuActionCmd3[menuActionRow] = child.id;
					menuActionRow++;
				}
				if(child.type == 2)
				{
					int k2 = 0;
					for(int l2 = 0; l2 < child.height; l2++)
					{
						for(int i3 = 0; i3 < child.width; i3++)
						{
							int j3 = xPos + i3 * (32 + child.invSpritePadX);
							int k3 = yPos + l2 * (32 + child.invSpritePadY);
							if(k2 < 20)
							{
								j3 += child.spritesX[k2];
								k3 += child.spritesY[k2];
							}
							if(mouse_x >= j3 && mouse_y >= k3 && mouse_x < j3 + 32 && mouse_y < k3 + 32)
							{
								mouseInvInterfaceIndex = k2;
								lastActiveInvInterface = child.id;
								if(child.inventory[k2] > 0)
								{
									ItemDef itemDef = ItemDef.getDef(child.inventory[k2] - 1);
									if(itemSelected == 1 && child.isInventoryInterface)
									{
										if(child.id != anInt1284 || k2 != anInt1283)
										{
											menuActionName[menuActionRow] = "Use " + selectedItemName + " with @lre@" + itemDef.name;
											menuActionID[menuActionRow] = 870;
											menuActionCmd1[menuActionRow] = itemDef.id;
											menuActionCmd2[menuActionRow] = k2;
											menuActionCmd3[menuActionRow] = child.id;
											menuActionRow++;
										}
									} else
										if(spellSelected == 1 && child.isInventoryInterface)
										{
											if((spellUsableOn & 0x10) == 16)
											{
												menuActionName[menuActionRow] = spellTooltip + " @lre@" + itemDef.name;
												menuActionID[menuActionRow] = 543;
												menuActionCmd1[menuActionRow] = itemDef.id;
												menuActionCmd2[menuActionRow] = k2;
												menuActionCmd3[menuActionRow] = child.id;
												menuActionRow++;
											}
										} else
										{
											if(child.isInventoryInterface)
											{
												for(int l3 = 4; l3 >= 3; l3--)
													if(itemDef.actions != null && itemDef.actions[l3] != null)
													{
														menuActionName[menuActionRow] = itemDef.actions[l3] + " @lre@" + itemDef.name;
														if(l3 == 3)
															menuActionID[menuActionRow] = 493;
														if(l3 == 4)
															menuActionID[menuActionRow] = 847;
														menuActionCmd1[menuActionRow] = itemDef.id;
														menuActionCmd2[menuActionRow] = k2;
														menuActionCmd3[menuActionRow] = child.id;
														menuActionRow++;
													} else
														if(l3 == 4)
														{
															menuActionName[menuActionRow] = "Drop @lre@" + itemDef.name;
															menuActionID[menuActionRow] = 847;
															menuActionCmd1[menuActionRow] = itemDef.id;
															menuActionCmd2[menuActionRow] = k2;
															menuActionCmd3[menuActionRow] = child.id;
															menuActionRow++;
														}

											}
											if(child.usableItemInterface)
											{
												menuActionName[menuActionRow] = "Use @lre@" + itemDef.name;
												menuActionID[menuActionRow] = 447;
												menuActionCmd1[menuActionRow] = itemDef.id;
												menuActionCmd2[menuActionRow] = k2;
												menuActionCmd3[menuActionRow] = child.id;
												menuActionRow++;
											}
											if(child.isInventoryInterface && itemDef.actions != null)
											{
												for(int i4 = 2; i4 >= 0; i4--)
													if(itemDef.actions[i4] != null)
													{
														menuActionName[menuActionRow] = itemDef.actions[i4] + " @lre@" + itemDef.name;
														if(i4 == 0)
															menuActionID[menuActionRow] = 74;
														if(i4 == 1)
															menuActionID[menuActionRow] = 454;
														if(i4 == 2)
															menuActionID[menuActionRow] = 539;
														menuActionCmd1[menuActionRow] = itemDef.id;
														menuActionCmd2[menuActionRow] = k2;
														menuActionCmd3[menuActionRow] = child.id;
														menuActionRow++;
													}

											}
											if(child.actions != null)
											{
												for(int j4 = 4; j4 >= 0; j4--)
													if(child.actions[j4] != null)
													{
														menuActionName[menuActionRow] = child.actions[j4] + " @lre@" + itemDef.name;
														if(j4 == 0)
															menuActionID[menuActionRow] = 632;
														if(j4 == 1)
															menuActionID[menuActionRow] = 78;
														if(j4 == 2)
															menuActionID[menuActionRow] = 867;
														if(j4 == 3)
															menuActionID[menuActionRow] = 431;
														if(j4 == 4)
															menuActionID[menuActionRow] = 53;
														menuActionCmd1[menuActionRow] = itemDef.id;
														menuActionCmd2[menuActionRow] = k2;
														menuActionCmd3[menuActionRow] = child.id;
														menuActionRow++;
													}

											}
											menuActionName[menuActionRow] = "Examine @lre@" + itemDef.name + " @gre@(@whi@" + (child.inventory[k2] - 1) + "@gre@)";
											menuActionID[menuActionRow] = 1125;
											menuActionCmd1[menuActionRow] = itemDef.id;
											menuActionCmd2[menuActionRow] = k2;
											menuActionCmd3[menuActionRow] = child.id;
											menuActionRow++;
										}
								}
							}
							k2++;
						}

					}

				}
			}
		}

	}

	private void drawScrollbar(int j, int k, int l, int i1, int j1)
	{
		scrollBar1.drawImage(i1, l);
		scrollBar2.drawImage(i1, (l + j) - 16);
		RSDrawingArea.drawFilledPixels(i1, l + 16, 16, j - 32, anInt1002);
		int k1 = ((j - 32) * j) / j1;
		if(k1 < 8)
			k1 = 8;
		int l1 = ((j - 32 - k1) * k) / (j1 - j);
		RSDrawingArea.drawFilledPixels(i1, l + 16 + l1, 16, k1, anInt1063);
		RSDrawingArea.drawVerticalLine(i1, l + 16 + l1, k1, anInt902);
		RSDrawingArea.drawVerticalLine(i1 + 1, l + 16 + l1, k1, anInt902);
		RSDrawingArea.drawHorizontalLine(i1, l + 16 + l1, 16, anInt902);
		RSDrawingArea.drawHorizontalLine(i1, l + 17 + l1, 16, anInt902);
		RSDrawingArea.drawVerticalLine(i1 + 15, l + 16 + l1, k1, anInt927);
		RSDrawingArea.drawVerticalLine(i1 + 14, l + 17 + l1, k1 - 1, anInt927);
		RSDrawingArea.drawHorizontalLine(i1, l + 15 + l1 + k1, 16, anInt927);
		RSDrawingArea.drawHorizontalLine(i1 + 1, l + 14 + l1 + k1, 15, anInt927);
	}

	private void updateNPCs(ByteBuffer in, int i) {
		anInt839 = 0;
		anInt893 = 0;
		method139(in);
		method46(i, in);
		handleNPCMasks(in);
		for(int k = 0; k < anInt839; k++) {
			int l = anIntArray840[k];
			if(npcArray[l].time != currentTime) {
				npcArray[l].desc = null;
				npcArray[l] = null;
			}
		}
		if(in.offset != i) {
			signlink.reportError(getUsername() + " size mismatch in getnpcpos - pos:" + in.offset + " psize:" + i);
			throw new RuntimeException("eek");
		}
		for(int i1 = 0; i1 < npcCount; i1++)
			if(npcArray[npcIndices[i1]] == null) {
				signlink.reportError(getUsername() + " null entry in npc list - pos:" + i1 + " size:" + npcCount);
				throw new RuntimeException("eek");
			}

	}

	public int currentChatMode = 0;
	public final int ALL = 0;
	public final int GAME = 1;
	public final int PUBLIC = 2;
	public final int PRIVATE = 3;
	public final int CLAN = 4;
	public final int YELL = 5;
	public final int TRADE = 6;
	public final int REPORT = 7;

	public int getCurrentChatMode() {
		return currentChatMode;
	}

	public void setCurrentChatMode(int mode) {
		currentChatMode = mode;
		inputTaken = true;
	}

	private void processChatModeClick() {
		if (getFrameVersion() == 317 && isFixed()) {
			if(super.clickMode3 == 1) {
				if (clickInRegion(6, 106, 467, 499)) {
					publicChatMode = (publicChatMode + 1) % 4;
					sendChatModes();
				}
				if (clickInRegion(135, 235, 467, 499)) {
					privateChatMode = (privateChatMode + 1) % 3;
					sendChatModes();
				}
				if (clickInRegion(273, 373, 467, 499)) {
					tradeMode = (tradeMode + 1) % 3;
					sendChatModes();
				}
				if (clickInRegion(412, 512, 467, 499)) {
					if(openInterfaceID == -1) {
						clearTopInterfaces();
						reportAbuseInput = "";
						canMute = false;
						for(int index = 0; index < RSInterface.cache.length; index++) {
							if(RSInterface.cache[index] == null || RSInterface.cache[index].contentType != 600) {
								continue;
							}
							reportAbuseInterfaceID = openInterfaceID = RSInterface.cache[index].parentId;
							break;
						}
					} else {
						pushMessage("", "Please close the interface you have open before using 'report abuse'", 0);
					}
				}
				anInt940++;
				if(anInt940 > 1386) {
					anInt940 = 0;
					out.putOpCode(165);
					out.writeWordBigEndian(0);
					int j = out.offset;
					out.writeWordBigEndian(139);
					out.writeWordBigEndian(150);
					out.putShort(32131);
					out.writeWordBigEndian((int)(Math.random() * 256D));
					out.putShort(3250);
					out.writeWordBigEndian(177);
					out.putShort(24859);
					out.writeWordBigEndian(119);
					if((int)(Math.random() * 2D) == 0) {
						out.putShort(47234);
					}
					if((int)(Math.random() * 2D) == 0) {
						out.writeWordBigEndian(21);
					}
					out.putBytes(out.offset - j);
				}
			}
		}
	}

	public void sendChatModes() {
		inputTaken = true;
		aBoolean1233 = true;
		out.putOpCode(PacketConstants.getSent().SEND_CHAT_MODES);
		out.writeWordBigEndian(publicChatMode);
		out.writeWordBigEndian(privateChatMode);
		out.writeWordBigEndian(tradeMode);
	}

	private void handleActions(int i)
	{
		int j = Varp.cache[i].anInt709;
		if(j == 0)
			return;
		int k = variousSettings[i];
		if(j == 1)
		{
			if(k == 1)
				Rasterizer.calculatePalette(0.90000000000000002D);
			if(k == 2)
				Rasterizer.calculatePalette(0.80000000000000004D);
			if(k == 3)
				Rasterizer.calculatePalette(0.69999999999999996D);
			if(k == 4)
				Rasterizer.calculatePalette(0.59999999999999998D);
			ItemDef.mruNodes1.unlinkAll();
			welcomeScreenRaised = true;
		}
		if(j == 3)
		{
			boolean flag1 = musicEnabled;
			if(k == 0)
			{
				adjustVolume(musicEnabled, 0);
				musicEnabled = true;
			}
			if(k == 1)
			{
				adjustVolume(musicEnabled, -400);
				musicEnabled = true;
			}
			if(k == 2)
			{
				adjustVolume(musicEnabled, -800);
				musicEnabled = true;
			}
			if(k == 3)
			{
				adjustVolume(musicEnabled, -1200);
				musicEnabled = true;
			}
			if(k == 4)
				musicEnabled = false;
			if(musicEnabled != flag1 && !lowMem)
			{
				if(musicEnabled)
				{
					nextSong = currentSong;
					songChanging = true;
					onDemandFetcher.method558(2, nextSong);
				} else
				{
					stopMidi();
				}
				prevSong = 0;
			}
		}
		if(j == 4)
		{
			if(k == 0)
			{
				aBoolean848 = true;
				setWaveVolume(0);
			}
			if(k == 1)
			{
				aBoolean848 = true;
				setWaveVolume(-400);
			}
			if(k == 2)
			{
				aBoolean848 = true;
				setWaveVolume(-800);
			}
			if(k == 3)
			{
				aBoolean848 = true;
				setWaveVolume(-1200);
			}
			if(k == 4)
				aBoolean848 = false;
		}
		if(j == 5)
			anInt1253 = k;
		if(j == 6)
			anInt1249 = k;
		if(j == 8)
		{
			splitPrivateChat = k;
			inputTaken = true;
		}
		if(j == 9)
			anInt913 = k;
	}

	private void updateEntities()
	{
		try{
			int anInt974 = 0;
			for(int j = -1; j < playerCount + npcCount; j++)
			{
				Object obj;
				if(j == -1)
					obj = myPlayer;
				else
					if(j < playerCount)
						obj = playerArray[playerIndices[j]];
					else
						obj = npcArray[npcIndices[j - playerCount]];
				if(obj == null || !((Entity) (obj)).isVisible())
					continue;
				if(obj instanceof NPC)
				{
					NPCDef entityDef = ((NPC)obj).desc;
					if(entityDef.childrenIDs != null)
						entityDef = entityDef.method161();
					if(entityDef == null)
						continue;
				}
				if(j < playerCount) {
					int l = 30;
					Player player = (Player)obj;
					if(player.prayerId != -1 && player.prayerId != 255 && player.prayerId != 65535) {
						npcScreenPos(((Entity) (obj)), ((Entity) (obj)).height + 15);
						if(spriteDrawX > -1) {
							headIcons[player.prayerId].drawImage(spriteDrawX - 12, spriteDrawY - l);
							l -= 25;
						}
					}
					if(j >= 0 && anInt855 == 10 && anInt933 == playerIndices[j]) {
						npcScreenPos(((Entity) (obj)), ((Entity) (obj)).height + 15);
						if(spriteDrawX > -1)
							headIcons[7].drawImage(spriteDrawX - 12, spriteDrawY - l);
					}
				} else {
					NPCDef entityDef_1 = ((NPC)obj).desc;
					if(entityDef_1.anInt75 >= 0 && entityDef_1.anInt75 < headIcons.length)
					{
						npcScreenPos(((Entity) (obj)), ((Entity) (obj)).height + 15);
						if(spriteDrawX > -1)
							headIcons[entityDef_1.anInt75].drawImage(spriteDrawX - 12, spriteDrawY - 30);
					}
					if(anInt855 == 1 && anInt1222 == npcIndices[j - playerCount] && currentTime % 20 < 10)
					{
						npcScreenPos(((Entity) (obj)), ((Entity) (obj)).height + 15);
						if(spriteDrawX > -1)
							headIcons[2].drawImage(spriteDrawX - 12, spriteDrawY - 28);
					}
				}
				if(((Entity) (obj)).textSpoken != null && (j >= playerCount || publicChatMode == 0 || publicChatMode == 3 || publicChatMode == 1 && isFriendOrSelf(((Player)obj).name)))
				{
					npcScreenPos(((Entity) (obj)), ((Entity) (obj)).height);
					if(spriteDrawX > -1 && anInt974 < anInt975)
					{
						anIntArray979[anInt974] = bold.getTextWidth(((Entity) (obj)).textSpoken) / 2;
						anIntArray978[anInt974] = bold.baseHeight;
						anIntArray976[anInt974] = spriteDrawX;
						anIntArray977[anInt974] = spriteDrawY;
						anIntArray980[anInt974] = ((Entity) (obj)).anInt1513;
						anIntArray981[anInt974] = ((Entity) (obj)).anInt1531;
						anIntArray982[anInt974] = ((Entity) (obj)).textCycle;
						aStringArray983[anInt974++] = ((Entity) (obj)).textSpoken;
						if(anInt1249 == 0 && ((Entity) (obj)).anInt1531 >= 1 && ((Entity) (obj)).anInt1531 <= 3)
						{
							anIntArray978[anInt974] += 10;
							anIntArray977[anInt974] += 5;
						}
						if(anInt1249 == 0 && ((Entity) (obj)).anInt1531 == 4)
							anIntArray979[anInt974] = 60;
						if(anInt1249 == 0 && ((Entity) (obj)).anInt1531 == 5)
							anIntArray978[anInt974] += 5;
					}
				}
				if(((Entity) (obj)).loopCycleStatus > currentTime)
				{ try{
					npcScreenPos(((Entity) (obj)), ((Entity) (obj)).height + 15);
					if(spriteDrawX > -1)
					{
						int i1 = (((Entity) (obj)).currentHealth * 30) / ((Entity) (obj)).maxHealth;
						if(i1 > 30)
							i1 = 30;
						RSDrawingArea.drawFilledPixels(spriteDrawX - 15, spriteDrawY - 3, i1, 5, 65280);
						RSDrawingArea.drawFilledPixels((spriteDrawX - 15) + i1, spriteDrawY - 3, 30 - i1, 5, 0xff0000);
					}
				}catch(Exception e){ }
				}
				for(int j1 = 0; j1 < 4; j1++)
					if(((Entity) (obj)).hitsLoopCycle[j1] > currentTime)
					{
						npcScreenPos(((Entity) (obj)), ((Entity) (obj)).height / 2);
						if(spriteDrawX > -1)
						{
							if(j1 == 1)
								spriteDrawY -= 20;
							if(j1 == 2)
							{
								spriteDrawX -= 15;
								spriteDrawY -= 10;
							}
							if(j1 == 3)
							{
								spriteDrawX += 15;
								spriteDrawY -= 10;
							}
							hitMarks[((Entity) (obj)).hitMarkTypes[j1]].drawImage(spriteDrawX - 12, spriteDrawY - 12);
							small.drawText(String.valueOf(((Entity) (obj)).hitDamage[j1]), spriteDrawX, spriteDrawY + 4, 0);
							small.drawText(String.valueOf(((Entity) (obj)).hitDamage[j1]), spriteDrawX - 1, spriteDrawY + 3, 0xffffff);
						}
					}

			}
			for(int k = 0; k < anInt974; k++)
			{
				int k1 = anIntArray976[k];
				int l1 = anIntArray977[k];
				int j2 = anIntArray979[k];
				int k2 = anIntArray978[k];
				boolean flag = true;
				while(flag) 
				{
					flag = false;
					for(int l2 = 0; l2 < k; l2++)
						if(l1 + 2 > anIntArray977[l2] - anIntArray978[l2] && l1 - k2 < anIntArray977[l2] + 2 && k1 - j2 < anIntArray976[l2] + anIntArray979[l2] && k1 + j2 > anIntArray976[l2] - anIntArray979[l2] && anIntArray977[l2] - anIntArray978[l2] < l1)
						{
							l1 = anIntArray977[l2] - anIntArray978[l2];
							flag = true;
						}

				}
				spriteDrawX = anIntArray976[k];
				spriteDrawY = anIntArray977[k] = l1;
				String s = aStringArray983[k];
				if(anInt1249 == 0)
				{
					int i3 = 0xffff00;
					if(anIntArray980[k] < 6)
						i3 = anIntArray965[anIntArray980[k]];
					if(anIntArray980[k] == 6)
						i3 = anInt1265 % 20 >= 10 ? 0xffff00 : 0xff0000;
						if(anIntArray980[k] == 7)
							i3 = anInt1265 % 20 >= 10 ? 65535 : 255;
							if(anIntArray980[k] == 8)
								i3 = anInt1265 % 20 >= 10 ? 0x80ff80 : 45056;
								if(anIntArray980[k] == 9)
								{
									int j3 = 150 - anIntArray982[k];
									if(j3 < 50)
										i3 = 0xff0000 + 1280 * j3;
									else
										if(j3 < 100)
											i3 = 0xffff00 - 0x50000 * (j3 - 50);
										else
											if(j3 < 150)
												i3 = 65280 + 5 * (j3 - 100);
								}
								if(anIntArray980[k] == 10)
								{
									int k3 = 150 - anIntArray982[k];
									if(k3 < 50)
										i3 = 0xff0000 + 5 * k3;
									else
										if(k3 < 100)
											i3 = 0xff00ff - 0x50000 * (k3 - 50);
										else
											if(k3 < 150)
												i3 = (255 + 0x50000 * (k3 - 100)) - 5 * (k3 - 100);
								}
								if(anIntArray980[k] == 11)
								{
									int l3 = 150 - anIntArray982[k];
									if(l3 < 50)
										i3 = 0xffffff - 0x50005 * l3;
									else
										if(l3 < 100)
											i3 = 65280 + 0x50005 * (l3 - 50);
										else
											if(l3 < 150)
												i3 = 0xffffff - 0x50000 * (l3 - 100);
								}
								if(anIntArray981[k] == 0)
								{
									bold.drawText(s, spriteDrawX, spriteDrawY + 1, 0);
									bold.drawText(s, spriteDrawX, spriteDrawY, i3);
								}
								if(anIntArray981[k] == 1)
								{
									bold.drawCenteredStringMoveY(s, spriteDrawX, spriteDrawY + 1, 0, anInt1265);
									bold.drawCenteredStringMoveY(s, spriteDrawX, spriteDrawY, i3, anInt1265);
								}
								if(anIntArray981[k] == 2)
								{
									bold.drawCenteredStringMoveXY(s, spriteDrawX, spriteDrawY + 1, 0, anInt1265);
									bold.drawCenteredStringMoveXY(s, spriteDrawX, spriteDrawY, i3, anInt1265);
								}
								if(anIntArray981[k] == 3)
								{
									bold.drawStringMoveY(150 - anIntArray982[k], s, anInt1265, spriteDrawY + 1, spriteDrawX, 0);
									bold.drawStringMoveY(150 - anIntArray982[k], s, anInt1265, spriteDrawY, spriteDrawX, i3);
								}
								if(anIntArray981[k] == 4)
								{
									int i4 = bold.getTextWidth(s);
									int k4 = ((150 - anIntArray982[k]) * (i4 + 100)) / 150;
									RSDrawingArea.setBounds(spriteDrawX - 50, spriteDrawX + 50, 0, 334);
									bold.drawBasicString(s, (spriteDrawX + 50) - k4, spriteDrawY + 1, 0);
									bold.drawBasicString(s, (spriteDrawX + 50) - k4, spriteDrawY, i3);
									RSDrawingArea.setDefaultArea();
								}
								if(anIntArray981[k] == 5)
								{
									int j4 = 150 - anIntArray982[k];
									int l4 = 0;
									if(j4 < 25)
										l4 = j4 - 25;
									else
										if(j4 > 125)
											l4 = j4 - 125;
									RSDrawingArea.setBounds(0, 512, spriteDrawY - bold.baseHeight - 1, spriteDrawY + 5);
									bold.drawText(s, spriteDrawX, spriteDrawY + 1 + l4, 0);
									bold.drawText(s, spriteDrawX, spriteDrawY + l4, i3);
									RSDrawingArea.setDefaultArea();
								}
				} else
				{
					bold.drawText(s, spriteDrawX, spriteDrawY + 1, 0);
					bold.drawText(s, spriteDrawX, spriteDrawY, 0xffff00);
				}
			}
		}catch(Exception e){ }

	}

	private void delFriend(long name) {
		try {
			if(name == 0L)
				return;
			for(int friendIndex = 0; friendIndex < friendsCount; friendIndex++) {
				if(friendsListAsLongs[friendIndex] != name) {
					continue;
				}
				friendsCount--;
				redrawTabArea = true;
				for(int index = friendIndex; index < friendsCount; index++) {
					friendsList[index] = friendsList[index + 1];
					friendsNodeIDs[index] = friendsNodeIDs[index + 1];
					friendsListAsLongs[index] = friendsListAsLongs[index + 1];
				}
				out.putOpCode(PacketConstants.getSent().DELETE_FRIEND);
				out.putLong(name);
				break;
			}
		} catch(RuntimeException runtimeexception) {
			signlink.reportError("18622, " + false + ", " + name + ", " + runtimeexception.toString());
			throw new RuntimeException();
		}
	}

	public int getTabWidth() {
		if (getFrameVersion() >= 554) {
			return 30;
		}
		return 33;
	}

	public int getTabHeight() {
		if (getFrameVersion() >= 554) {
			return 37;
		}
		return 36;
	}

	public int getTabRowHeight() {
		return getClientWidth() >= 977 ? getTabHeight() : getTabHeight() * 2;
	}

	public void drawTabs() {
		RSImage[] tab = { new RSImage(getFrameVersion() >= 554 ? "tab_554" : "tab"), new RSImage(getFrameVersion() >= 554 ? "tab_clicked_554" : "tab_clicked") };
		RSImage outline = new RSImage("tab_clicked_outline");
		RSImage glow = new RSImage("tab_clicked_glow");
		int startX = getClientWidth() - getTabWidth() * (getTabRowHeight() > 40 ? 7 : 14);
		for (int index = 0; index < 7; index++, startX += getTabWidth()) {
			tab[index == tabID && index != 10 ? 1 : 0].drawImage(startX, getClientHeight() - getTabRowHeight());
		}
		startX = getClientWidth() - getTabWidth() * 7;
		for (int index = 7; index < 14; index++, startX += getTabWidth()) {
			tab[index == tabID && index != 10 ? 1 : 0].drawImage(startX, getClientHeight() - (getTabRowHeight() > 40 ? getTabRowHeight() / 2 : getTabRowHeight()));
		}
		startX = getClientWidth() - getTabWidth() * (getTabRowHeight() > 40 ? 7 : 14);
		if (getFrameVersion() >= 554) {
			for (int index = 0; index < 7; index++, startX += getTabWidth()) {
				if (index == tabID && index != 10) {
					glow.drawCenteredARGBImage(startX + (getTabWidth() / 2), getClientHeight() - getTabRowHeight() + (getTabHeight() / 2) + 1);
					outline.drawImage(startX, getClientHeight() - getTabRowHeight());
				}
			}
			startX = getClientWidth() - getTabWidth() * 7;
			for (int index = 7; index < 14; index++, startX += getTabWidth()) {
				if (index == tabID && index != 10) {
					glow.drawCenteredARGBImage(startX + (getTabWidth() / 2), getClientHeight() - (getTabRowHeight() / 2) + (getTabRowHeight() > 40 ? getTabHeight() / 2 : 0) + 1);
					outline.drawImage(startX, getClientHeight() - (getTabRowHeight() > 40 ? getTabRowHeight() / 2 : getTabRowHeight()));
				}
			}
		}
	}

	public void drawSideIcons() {
		int startX = getClientWidth() - getTabWidth() * (getTabRowHeight() > 40 ? 7 : 14);
		for (int index = 0; index < 7; index++, startX += getTabWidth()) {
			if (tabInterfaceIDs[index] != -1 && index != 10) {
				RSImage icon = new RSImage("sideicons/" + index + (getFrameVersion() >= 554 ? "_554" : ""));
				icon.drawCenteredImage(startX + (getTabWidth() / 2) + (getFrameVersion() >= 554 ? 0 : 1), getClientHeight() - getTabRowHeight() + (getTabHeight() / 2));
			}
		}
		startX = getClientWidth() - getTabWidth() * 7;
		for (int index = 7; index < 14; index++, startX += getTabWidth()) {
			if (tabInterfaceIDs[index] != -1 && index != 10) {
				RSImage icon = new RSImage("sideicons/" + index + (getFrameVersion() >= 554 ? "_554" : ""));
				icon.drawCenteredImage(startX + (getTabWidth() / 2) + (getFrameVersion() >= 554 ? 0 : 1), getClientHeight() - (getTabRowHeight() > 40 ? getTabRowHeight() / 2 : getTabRowHeight()) + (getTabHeight() / 2));
			}
		}
	}

	private void drawTabArea() {
		if (isFixed()) {
			tabArea.initDrawingArea();
		}
		Rasterizer.lineOffsets = tabAreaTextureArray;
		if (isFixed() && getFrameVersion() == 317) {
			invBack.drawImage(0, 0);
			if(invOverlayInterfaceID != -1) {
				drawInterface(RSInterface.cache[invOverlayInterfaceID], 0, 0, 0);
			} else if(tabInterfaceIDs[tabID] != -1) {
				drawInterface(RSInterface.cache[tabInterfaceIDs[tabID]], 0, 0, 0);
			}
		} else if (!isFixed()) {
			RSImage back = new RSImage("invback");
			RSImage border = new RSImage("invborder");
			back.drawImage(getClientWidth() - 204 + 7, getClientHeight() - 274 - (getTabRowHeight() - 7), 156);
			border.drawImage(getClientWidth() - 204, getClientHeight() - 274 - getTabRowHeight());
			if(invOverlayInterfaceID != -1) {
				drawInterface(RSInterface.cache[invOverlayInterfaceID], getClientWidth() - 204 + 7, getClientHeight() - 274 - (getTabRowHeight() - 7), 0);
			} else if(tabInterfaceIDs[tabID] != -1) {
				drawInterface(RSInterface.cache[tabInterfaceIDs[tabID]], getClientWidth() - 204 + 7, getClientHeight() - 274 - (getTabRowHeight() - 7), 0);
			}
			drawTabs();
			drawSideIcons();
		}
		if(menuOpen && menuScreenArea == 1) {
			drawMenu();
		}
		if (isFixed()) {
			tabArea.drawGraphics(553, 205, super.graphics);
		}
		gameArea.initDrawingArea();
		Rasterizer.lineOffsets = gameAreaTextureArray;
	}

	private void handleTextureMovement(int id) {
		if(!lowMem) {
			if(Rasterizer.textureLastUsed[17] >= id) {
				IndexedImage image = Rasterizer.textureImages[17];
				int totalPixels = image.myWidth * image.myHeight - 1;
				int speed = image.myWidth * anInt945 * 5;
				byte pixels[] = image.myPixels;
				byte newPixels[] = texturePixels;
				for(int index = 0; index <= totalPixels; index++) {
					newPixels[index] = pixels[index - speed & totalPixels];
				}
				image.myPixels = newPixels;
				texturePixels = pixels;
				Rasterizer.resetTexture(17);
				/*
				 * Seems to be some sort of anti-bot packet or something of that sort.
				 * Has no practical usage in a 317 private server, so I've commented it out.
				 */
				/*anInt854++;
				if(anInt854 > 1235) {
					anInt854 = 0;
					out.putOpCode(226);
					out.writeWordBigEndian(0);
					int l2 = out.offset;
					out.putShort(58722);
					out.writeWordBigEndian(240);
					out.putShort((int)(Math.random() * 65536D));
					out.writeWordBigEndian((int)(Math.random() * 256D));
					if((int)(Math.random() * 2D) == 0) {
						out.putShort(51825);
					}
					out.writeWordBigEndian((int)(Math.random() * 256D));
					out.putShort((int)(Math.random() * 65536D));
					out.putShort(7130);
					out.putShort((int)(Math.random() * 65536D));
					out.putShort(61657);
					out.putBytes(out.offset - l2);
				}*/
			}
			if(Rasterizer.textureLastUsed[24] >= id) {
				IndexedImage image = Rasterizer.textureImages[24];
				int totalPixels = image.myWidth * image.myHeight - 1;
				int speed = image.myWidth * anInt945 * 2;
				byte pixels[] = image.myPixels;
				byte newPixels[] = texturePixels;
				for(int index = 0; index <= totalPixels; index++) {
					newPixels[index] = pixels[index - speed & totalPixels];
				}
				image.myPixels = newPixels;
				texturePixels = pixels;
				Rasterizer.resetTexture(24);
			}
			if(Rasterizer.textureLastUsed[34] >= id) {
				IndexedImage image = Rasterizer.textureImages[34];
				int totalPixels = image.myWidth * image.myHeight - 1;
				int speed = image.myWidth * anInt945 * 2;
				byte pixels[] = image.myPixels;
				byte newPixels[] = texturePixels;
				for(int k2 = 0; k2 <= totalPixels; k2++) {
					newPixels[k2] = pixels[k2 - speed & totalPixels];
				}
				image.myPixels = newPixels;
				texturePixels = pixels;
				Rasterizer.resetTexture(34);
			}
			if(Rasterizer.textureLastUsed[40] >= id) {
				IndexedImage image = Rasterizer.textureImages[40];
				int totalPixels = image.myWidth * image.myHeight - 1;
				int speed = image.myWidth * anInt945 * 2;
				byte pixels[] = image.myPixels;
				byte newPixels[] = texturePixels;
				for(int index = 0; index <= totalPixels; index++) {
					newPixels[index] = pixels[index - speed & totalPixels];
				}
				image.myPixels = newPixels;
				texturePixels = pixels;
				Rasterizer.resetTexture(40);
			}
			/*for (int index = 0; index < 50; index++) {
				if(Rasterizer.textureLastUsed[index] >= id) {
					IndexedImage image = Rasterizer.textureImages[index];
					int totalPixels = image.myWidth * image.myHeight - 1;
					int speed = image.myWidth * anInt945 * 2;
					byte pixels[] = image.myPixels;
					byte newPixels[] = texturePixels;
					for(int pointer = 0; pointer <= totalPixels; pointer++) {
						newPixels[pointer] = pixels[pointer - speed & totalPixels];
					}
					image.myPixels = newPixels;
					texturePixels = pixels;
					Rasterizer.resetTexture(index);
				}
			}*/
		}
	}

	private void method38()
	{
		for(int i = -1; i < playerCount; i++)
		{
			int j;
			if(i == -1)
				j = myPlayerIndex;
			else
				j = playerIndices[i];
			Player player = playerArray[j];
			if(player != null && player.textCycle > 0)
			{
				player.textCycle--;
				if(player.textCycle == 0)
					player.textSpoken = null;
			}
		}

		for(int k = 0; k < npcCount; k++)
		{
			int l = npcIndices[k];
			NPC npc = npcArray[l];
			if(npc != null && npc.textCycle > 0)
			{
				npc.textCycle--;
				if(npc.textCycle == 0)
					npc.textSpoken = null;
			}
		}

	}

	private void calcCameraPos()
	{
		int i = anInt1098 * 128 + 64;
		int j = anInt1099 * 128 + 64;
		int k = method42(floor_level, j, i) - anInt1100;
		if(xCameraPos < i)
		{
			xCameraPos += anInt1101 + ((i - xCameraPos) * anInt1102) / 1000;
			if(xCameraPos > i)
				xCameraPos = i;
		}
		if(xCameraPos > i)
		{
			xCameraPos -= anInt1101 + ((xCameraPos - i) * anInt1102) / 1000;
			if(xCameraPos < i)
				xCameraPos = i;
		}
		if(zCameraPos < k)
		{
			zCameraPos += anInt1101 + ((k - zCameraPos) * anInt1102) / 1000;
			if(zCameraPos > k)
				zCameraPos = k;
		}
		if(zCameraPos > k)
		{
			zCameraPos -= anInt1101 + ((zCameraPos - k) * anInt1102) / 1000;
			if(zCameraPos < k)
				zCameraPos = k;
		}
		if(yCameraPos < j)
		{
			yCameraPos += anInt1101 + ((j - yCameraPos) * anInt1102) / 1000;
			if(yCameraPos > j)
				yCameraPos = j;
		}
		if(yCameraPos > j)
		{
			yCameraPos -= anInt1101 + ((yCameraPos - j) * anInt1102) / 1000;
			if(yCameraPos < j)
				yCameraPos = j;
		}
		i = anInt995 * 128 + 64;
		j = anInt996 * 128 + 64;
		k = method42(floor_level, j, i) - anInt997;
		int l = i - xCameraPos;
		int i1 = k - zCameraPos;
		int j1 = j - yCameraPos;
		int k1 = (int)Math.sqrt(l * l + j1 * j1);
		int l1 = (int)(Math.atan2(i1, k1) * 325.94900000000001D) & 0x7ff;
		int i2 = (int)(Math.atan2(l, j1) * -325.94900000000001D) & 0x7ff;
		if(l1 < 128)
			l1 = 128;
		if(l1 > 383)
			l1 = 383;
		if(yCameraCurve < l1)
		{
			yCameraCurve += anInt998 + ((l1 - yCameraCurve) * anInt999) / 1000;
			if(yCameraCurve > l1)
				yCameraCurve = l1;
		}
		if(yCameraCurve > l1)
		{
			yCameraCurve -= anInt998 + ((yCameraCurve - l1) * anInt999) / 1000;
			if(yCameraCurve < l1)
				yCameraCurve = l1;
		}
		int j2 = i2 - xCameraCurve;
		if(j2 > 1024)
			j2 -= 2048;
		if(j2 < -1024)
			j2 += 2048;
		if(j2 > 0)
		{
			xCameraCurve += anInt998 + (j2 * anInt999) / 1000;
			xCameraCurve &= 0x7ff;
		}
		if(j2 < 0)
		{
			xCameraCurve -= anInt998 + (-j2 * anInt999) / 1000;
			xCameraCurve &= 0x7ff;
		}
		int k2 = i2 - xCameraCurve;
		if(k2 > 1024)
			k2 -= 2048;
		if(k2 < -1024)
			k2 += 2048;
		if(k2 < 0 && j2 > 0 || k2 > 0 && j2 < 0)
			xCameraCurve = i2;
	}

	private void drawMenu()
	{
		int x = menuOffsetX;
		int y = menuOffsetY;
		int width = menuWidth;
		int height = menuHeight;
		int color = 0x5d5447;
		RSDrawingArea.drawFilledPixels(x, y, width, height, color);
		RSDrawingArea.drawFilledPixels(x + 1, y + 1, width - 2, 16, 0);
		RSDrawingArea.drawUnfilledPixels(x + 1, y + 18, width - 2, height - 19, 0);
		bold.drawBasicString("Choose Option", x + 3, y + 14, color);
		int j1 = super.mouseX;
		int k1 = super.mouseY;
		if(menuScreenArea == 0)
		{
			j1 -= 4;
			k1 -= 4;
		}
		if(menuScreenArea == 1)
		{
			j1 -= 553;
			k1 -= 205;
		}
		if(menuScreenArea == 2)
		{
			j1 -= 17;
			k1 -= 357;
		}
		for(int l1 = 0; l1 < menuActionRow; l1++)
		{
			int i2 = y + 31 + (menuActionRow - 1 - l1) * 15;
			int j2 = 0xffffff;
			if(j1 > x && j1 < x + width && k1 > i2 - 13 && k1 < i2 + 3)
				j2 = 0xffff00;
			bold.drawShadowedString(menuActionName[l1], x + 3, i2, j2, true);
		}

	}

	private void addFriend(long l)
	{
		try
		{
			if(l == 0L)
				return;
			if(friendsCount >= 100 && anInt1046 != 1)
			{
				pushMessage("", "Your friendlist is full. Max of 100 for free users, and 200 for members", 0);
				return;
			}
			if(friendsCount >= 200)
			{
				pushMessage("", "Your friendlist is full. Max of 100 for free users, and 200 for members", 0);
				return;
			}
			String s = TextUtils.fixName(TextUtils.nameForLong(l));
			for(int i = 0; i < friendsCount; i++)
				if(friendsListAsLongs[i] == l)
				{
					pushMessage("", s + " is already on your friend list", 0);
					return;
				}
			for(int j = 0; j < ignoreCount; j++)
				if(ignoreListAsLongs[j] == l)
				{
					pushMessage("", "Please remove " + s + " from your ignore list first", 0);
					return;
				}

			if(s.equals(myPlayer.name))
			{
				return;
			} else
			{
				friendsList[friendsCount] = s;
				friendsListAsLongs[friendsCount] = l;
				friendsNodeIDs[friendsCount] = 0;
				friendsCount++;
				redrawTabArea = true;
				out.putOpCode(188);
				out.putLong(l);
				return;
			}
		}
		catch(RuntimeException runtimeexception)
		{
			signlink.reportError("15283, " + (byte)68 + ", " + l + ", " + runtimeexception.toString());
		}
		throw new RuntimeException();
	}

	private int method42(int i, int j, int k)
	{
		int l = k >> 7;
			int i1 = j >> 7;
											if(l < 0 || i1 < 0 || l > 103 || i1 > 103)
												return 0;
											int j1 = i;
											if(j1 < 3 && (byteGroundArray[1][l][i1] & 2) == 2)
												j1++;
											int k1 = k & 0x7f;
											int l1 = j & 0x7f;
											int i2 = intGroundArray[j1][l][i1] * (128 - k1) + intGroundArray[j1][l + 1][i1] * k1 >> 7;
											int j2 = intGroundArray[j1][l][i1 + 1] * (128 - k1) + intGroundArray[j1][l + 1][i1 + 1] * k1 >> 7;
											return i2 * (128 - l1) + j2 * l1 >> 7;
	}

	private static String intToKOrMil(int j)
	{
		if(j < 0x186a0)
			return String.valueOf(j);
		if(j < 0x989680)
			return j / 1000 + "K";
		else
			return j / 0xf4240 + "M";
	}

	private void resetLogout()
	{
		try
		{
			if(socketStream != null)
				socketStream.close();
		}
		catch(Exception _ex) { }
		socketStream = null;
		loggedIn = false;
		loginScreenState = LOGIN;
		loginMessage1 = "Please enter your login details.";
		loginMessage2 = "";
		unlinkMRUNodes();
		worldController.initToNull();
		for(int i = 0; i < 4; i++)
			aClass11Array1230[i].init();

		System.gc();
		stopMidi();
		currentSong = -1;
		nextSong = -1;
		prevSong = 0;
	}

	private void method45()
	{
		aBoolean1031 = true;
		for(int j = 0; j < 7; j++)
		{
			anIntArray1065[j] = -1;
			for(int k = 0; k < IdentityKit.length; k++)
			{
				if(IdentityKit.cache[k].aBoolean662 || IdentityKit.cache[k].anInt657 != j + (aBoolean1047 ? 0 : 7))
					continue;
				anIntArray1065[j] = k;
				break;
			}

		}

	}

	private void method46(int i, ByteBuffer stream)
	{
		while(stream.pos + 21 < i * 8)
		{
			int k = stream.getBits(14);
			if(k == 16383)
				break;
			if(npcArray[k] == null)
				npcArray[k] = new NPC();
			NPC npc = npcArray[k];
			npcIndices[npcCount++] = k;
			npc.time = currentTime;
			int l = stream.getBits(5);
			if(l > 15)
				l -= 32;
			int i1 = stream.getBits(5);
			if(i1 > 15)
				i1 -= 32;
			int j1 = stream.getBits(1);
			npc.desc = NPCDef.getDef(stream.getBits(12));
			int k1 = stream.getBits(1);
			if(k1 == 1)
				anIntArray894[anInt893++] = k;
			npc.boundDim = npc.desc.aByte68;
			npc.degreesToTurn = npc.desc.anInt79;
			npc.walkAnimIndex = npc.desc.anInt67;
			npc.turn180AnimIndex = npc.desc.anInt58;
			npc.turn90CWAnimIndex = npc.desc.anInt83;
			npc.turn90CCWAnimIndex = npc.desc.anInt55;
			npc.standAnimIndex = npc.desc.anInt77;
			npc.setPos(myPlayer.pathX[0] + i1, myPlayer.pathY[0] + l, j1 == 1);
		}
		stream.finishBitAccess();
	}

	public void processGameLoop()
	{
		if(rsAlreadyLoaded || loadingError || genericLoadingError)
			return;
		currentTime++;
		if(!loggedIn)
			processLoginScreenInput();
		else
			mainGameProcessor();
		processOnDemandQueue();
	}

	private void method47(boolean flag)
	{
		if(myPlayer.currentX >> 7 == destX && myPlayer.currentY >> 7 == destY)
			destX = 0;
		int j = playerCount;
		if(flag)
			j = 1;
		for(int l = 0; l < j; l++)
		{
			Player player;
			int i1;
			if(flag)
			{
				player = myPlayer;
				i1 = myPlayerIndex << 14;
			} else
			{
				player = playerArray[playerIndices[l]];
				i1 = playerIndices[l] << 14;
			}
			if(player == null || !player.isVisible())
				continue;
			player.aBoolean1699 = (lowMem && playerCount > 50 || playerCount > 200) && !flag && player.anInt1517 == player.standAnimIndex;
			int j1 = player.currentX >> 7;
				int k1 = player.currentY >> 7;
		if(j1 < 0 || j1 >= 104 || k1 < 0 || k1 >= 104)
			continue;
		if(player.aModel_1714 != null && currentTime >= player.anInt1707 && currentTime < player.anInt1708)
		{
			player.aBoolean1699 = false;
			player.anInt1709 = method42(floor_level, player.currentY, player.currentX);
			worldController.method286(floor_level, player.currentY, player, player.currentRotation, player.anInt1722, player.currentX, player.anInt1709, player.anInt1719, player.anInt1721, i1, player.anInt1720);
			continue;
		}
		if((player.currentX & 0x7f) == 64 && (player.currentY & 0x7f) == 64)
		{
			if(anIntArrayArray929[j1][k1] == anInt1265)
				continue;
			anIntArrayArray929[j1][k1] = anInt1265;
		}
		player.anInt1709 = method42(floor_level, player.currentY, player.currentX);
		worldController.method285(floor_level, player.currentRotation, player.anInt1709, i1, player.currentY, 60, player.currentX, player, player.aBoolean1541);
		}

	}

	private boolean promptUserForInput(RSInterface class9)
	{
		int j = class9.contentType;
		if(anInt900 == 2)
		{
			if(j == 201)
			{
				inputTaken = true;
				dialogState = 0;
				promptRaised = true;
				promptInput = "";
				friendsListAction = 1;
				promptMessage = "Enter name of friend to add to list";
			}
			if(j == 202)
			{
				inputTaken = true;
				dialogState = 0;
				promptRaised = true;
				promptInput = "";
				friendsListAction = 2;
				promptMessage = "Enter name of friend to delete from list";
			}
		}
		if(j == 205)
		{
			anInt1011 = 250;
			return true;
		}
		if(j == 501)
		{
			inputTaken = true;
			dialogState = 0;
			promptRaised = true;
			promptInput = "";
			friendsListAction = 4;
			promptMessage = "Enter name of player to add to list";
		}
		if(j == 502)
		{
			inputTaken = true;
			dialogState = 0;
			promptRaised = true;
			promptInput = "";
			friendsListAction = 5;
			promptMessage = "Enter name of player to delete from list";
		}
		if(j >= 300 && j <= 313)
		{
			int k = (j - 300) / 2;
			int j1 = j & 1;
			int i2 = anIntArray1065[k];
			if(i2 != -1)
			{
				do
				{
					if(j1 == 0 && --i2 < 0)
						i2 = IdentityKit.length - 1;
					if(j1 == 1 && ++i2 >= IdentityKit.length)
						i2 = 0;
				} while(IdentityKit.cache[i2].aBoolean662 || IdentityKit.cache[i2].anInt657 != k + (aBoolean1047 ? 0 : 7));
				anIntArray1065[k] = i2;
				aBoolean1031 = true;
			}
		}
		if(j >= 314 && j <= 323)
		{
			int l = (j - 314) / 2;
			int k1 = j & 1;
			int j2 = anIntArray990[l];
			if(k1 == 0 && --j2 < 0)
				j2 = anIntArrayArray1003[l].length - 1;
			if(k1 == 1 && ++j2 >= anIntArrayArray1003[l].length)
				j2 = 0;
			anIntArray990[l] = j2;
			aBoolean1031 = true;
		}
		if(j == 324 && !aBoolean1047)
		{
			aBoolean1047 = true;
			method45();
		}
		if(j == 325 && aBoolean1047)
		{
			aBoolean1047 = false;
			method45();
		}
		if(j == 326)
		{
			out.putOpCode(101);
			out.writeWordBigEndian(aBoolean1047 ? 0 : 1);
			for(int i1 = 0; i1 < 7; i1++)
				out.writeWordBigEndian(anIntArray1065[i1]);

			for(int l1 = 0; l1 < 5; l1++)
				out.writeWordBigEndian(anIntArray990[l1]);

			return true;
		}
		if(j == 613)
			canMute = !canMute;
		if(j >= 601 && j <= 612)
		{
			clearTopInterfaces();
			if(reportAbuseInput.length() > 0)
			{
				out.putOpCode(218);
				out.putLong(TextUtils.longForName(reportAbuseInput));
				out.writeWordBigEndian(j - 601);
				out.writeWordBigEndian(canMute ? 1 : 0);
			}
		}
		return false;
	}

	private void method49(ByteBuffer stream)
	{
		for(int j = 0; j < anInt893; j++)
		{
			int k = anIntArray894[j];
			Player player = playerArray[k];
			int l = stream.getUByte();
			if((l & 0x40) != 0)
				l += stream.getUByte() << 8;
			method107(l, k, stream, player);
		}

	}

	private void method50(int i, int k, int l, int i1, int j1)
	{
		int k1 = worldController.getWallObjectUID(j1, l, i);
		if(k1 != 0)
		{
			int l1 = worldController.getIdTagForPosition(j1, l, i, k1);
			int k2 = l1 >> 6 & 3;
			int i3 = l1 & 0x1f;
			int k3 = k;
			if(k1 > 0)
				k3 = i1;
			int ai[] = aClass30_Sub2_Sub1_Sub1_1263.myPixels;
			int k4 = 24624 + l * 4 + (103 - i) * 512 * 4;
			int i5 = k1 >> 14 & 0x7fff;
			ObjectDef class46_2 = ObjectDef.getDef(i5);
			if(class46_2.anInt758 != -1)
			{
				IndexedImage background_2 = mapScenes[class46_2.anInt758];
				if(background_2 != null)
				{
					int i6 = (class46_2.sizeX * 4 - background_2.myWidth) / 2;
					int j6 = (class46_2.sizeY * 4 - background_2.myHeight) / 2;
					background_2.drawImage(48 + l * 4 + i6, 48 + (104 - i - class46_2.sizeY) * 4 + j6);
				}
			} else
			{
				if(i3 == 0 || i3 == 2)
					if(k2 == 0)
					{
						ai[k4] = k3;
						ai[k4 + 512] = k3;
						ai[k4 + 1024] = k3;
						ai[k4 + 1536] = k3;
					} else
						if(k2 == 1)
						{
							ai[k4] = k3;
							ai[k4 + 1] = k3;
							ai[k4 + 2] = k3;
							ai[k4 + 3] = k3;
						} else
							if(k2 == 2)
							{
								ai[k4 + 3] = k3;
								ai[k4 + 3 + 512] = k3;
								ai[k4 + 3 + 1024] = k3;
								ai[k4 + 3 + 1536] = k3;
							} else
								if(k2 == 3)
								{
									ai[k4 + 1536] = k3;
									ai[k4 + 1536 + 1] = k3;
									ai[k4 + 1536 + 2] = k3;
									ai[k4 + 1536 + 3] = k3;
								}
				if(i3 == 3)
					if(k2 == 0)
						ai[k4] = k3;
					else
						if(k2 == 1)
							ai[k4 + 3] = k3;
						else
							if(k2 == 2)
								ai[k4 + 3 + 1536] = k3;
							else
								if(k2 == 3)
									ai[k4 + 1536] = k3;
				if(i3 == 2)
					if(k2 == 3)
					{
						ai[k4] = k3;
						ai[k4 + 512] = k3;
						ai[k4 + 1024] = k3;
						ai[k4 + 1536] = k3;
					} else
						if(k2 == 0)
						{
							ai[k4] = k3;
							ai[k4 + 1] = k3;
							ai[k4 + 2] = k3;
							ai[k4 + 3] = k3;
						} else
							if(k2 == 1)
							{
								ai[k4 + 3] = k3;
								ai[k4 + 3 + 512] = k3;
								ai[k4 + 3 + 1024] = k3;
								ai[k4 + 3 + 1536] = k3;
							} else
								if(k2 == 2)
								{
									ai[k4 + 1536] = k3;
									ai[k4 + 1536 + 1] = k3;
									ai[k4 + 1536 + 2] = k3;
									ai[k4 + 1536 + 3] = k3;
								}
			}
		}
		k1 = worldController.getInteractableObjectUID(j1, l, i);
		if(k1 != 0)
		{
			int i2 = worldController.getIdTagForPosition(j1, l, i, k1);
			int l2 = i2 >> 6 & 3;
			int j3 = i2 & 0x1f;
			int l3 = k1 >> 14 & 0x7fff;
		ObjectDef class46_1 = ObjectDef.getDef(l3);
		if(class46_1.anInt758 != -1)
		{
			IndexedImage background_1 = mapScenes[class46_1.anInt758];
			if(background_1 != null)
			{
				int j5 = (class46_1.sizeX * 4 - background_1.myWidth) / 2;
				int k5 = (class46_1.sizeY * 4 - background_1.myHeight) / 2;
				background_1.drawImage(48 + l * 4 + j5, 48 + (104 - i - class46_1.sizeY) * 4 + k5);
			}
		} else
			if(j3 == 9)
			{
				int l4 = 0xeeeeee;
				if(k1 > 0)
					l4 = 0xee0000;
				int ai1[] = aClass30_Sub2_Sub1_Sub1_1263.myPixels;
				int l5 = 24624 + l * 4 + (103 - i) * 512 * 4;
				if(l2 == 0 || l2 == 2)
				{
					ai1[l5 + 1536] = l4;
					ai1[l5 + 1024 + 1] = l4;
					ai1[l5 + 512 + 2] = l4;
					ai1[l5 + 3] = l4;
				} else
				{
					ai1[l5] = l4;
					ai1[l5 + 512 + 1] = l4;
					ai1[l5 + 1024 + 2] = l4;
					ai1[l5 + 1536 + 3] = l4;
				}
			}
		}
		k1 = worldController.getGroundDecorationUID(j1, l, i);
		if(k1 != 0)
		{
			int j2 = k1 >> 14 & 0x7fff;
		ObjectDef class46 = ObjectDef.getDef(j2);
		if(class46.anInt758 != -1)
		{
			IndexedImage background = mapScenes[class46.anInt758];
			if(background != null)
			{
				int i4 = (class46.sizeX * 4 - background.myWidth) / 2;
				int j4 = (class46.sizeY * 4 - background.myHeight) / 2;
				background.drawImage(48 + l * 4 + i4, 48 + (104 - i - class46.sizeY) * 4 + j4);
			}
		}
		}
	}

	private void loadTitleScreen() {
		//titleBox = new IndexedImage(titleStreamLoader, "titlebox", 0);
		//titleButton = new IndexedImage(titleStreamLoader, "titlebutton", 0);
		background[0] = new RSImage("title", 0, 0, 383, 252);
		background[1] = new RSImage("title", 383, 0, 382, 252);
		background[2] = new RSImage("title", 0, 252, 383, 251);
		background[3] = new RSImage("title", 383, 252, 382, 251);
		aBackgroundArray1152s = new IndexedImage[12];
		int j = 0;
		try
		{
			j = Integer.parseInt(getParameter("fl_icon"));
		}
		catch(Exception _ex) { }
		if(j == 0)
		{
			for(int k = 0; k < 12; k++)
				aBackgroundArray1152s[k] = new IndexedImage(titleArchive, "runes", k);

		} else
		{
			for(int l = 0; l < 12; l++)
				aBackgroundArray1152s[l] = new IndexedImage(titleArchive, "runes", 12 + (l & 3));

		}
		aClass30_Sub2_Sub1_Sub1_1201 = new RSImage(128, 265);
		aClass30_Sub2_Sub1_Sub1_1202 = new RSImage(128, 265);
		System.arraycopy(leftFlames.anIntArray315, 0, aClass30_Sub2_Sub1_Sub1_1201.myPixels, 0, 33920);

		System.arraycopy(rightFlames.anIntArray315, 0, aClass30_Sub2_Sub1_Sub1_1202.myPixels, 0, 33920);

		anIntArray851 = new int[256];
		for(int k1 = 0; k1 < 64; k1++)
			anIntArray851[k1] = k1 * 0x40000;

		for(int l1 = 0; l1 < 64; l1++)
			anIntArray851[l1 + 64] = 0xff0000 + 1024 * l1;

		for(int i2 = 0; i2 < 64; i2++)
			anIntArray851[i2 + 128] = 0xffff00 + 4 * i2;

		for(int j2 = 0; j2 < 64; j2++)
			anIntArray851[j2 + 192] = 0xffffff;

		anIntArray852 = new int[256];
		for(int k2 = 0; k2 < 64; k2++)
			anIntArray852[k2] = k2 * 1024;

		for(int l2 = 0; l2 < 64; l2++)
			anIntArray852[l2 + 64] = 65280 + 4 * l2;

		for(int i3 = 0; i3 < 64; i3++)
			anIntArray852[i3 + 128] = 65535 + 0x40000 * i3;

		for(int j3 = 0; j3 < 64; j3++)
			anIntArray852[j3 + 192] = 0xffffff;

		anIntArray853 = new int[256];
		for(int k3 = 0; k3 < 64; k3++)
			anIntArray853[k3] = k3 * 4;

		for(int l3 = 0; l3 < 64; l3++)
			anIntArray853[l3 + 64] = 255 + 0x40000 * l3;

		for(int i4 = 0; i4 < 64; i4++)
			anIntArray853[i4 + 128] = 0xff00ff + 1024 * i4;

		for(int j4 = 0; j4 < 64; j4++)
			anIntArray853[j4 + 192] = 0xffffff;

		anIntArray850 = new int[256];
		anIntArray1190 = new int[32768];
		anIntArray1191 = new int[32768];
		randomizeBackground(null);
		anIntArray828 = new int[32768];
		anIntArray829 = new int[32768];
		displayProgress("Connecting to fileserver", 10);
		if(!aBoolean831)
		{
			drawFlames = true;
			aBoolean831 = true;
			startRunnable(this, 2);
		}
	}

	private static void setHighMem()
	{
		SceneGraph.lowMem = false;
		Rasterizer.lowMem = false;
		lowMem = false;
		MapRegion.lowMem = false;
		ObjectDef.lowMem = false;
	}

	public static Client instance;
	public static Client getClient() {
		return instance;
	}

	public static void main(String args[]) {
		try {
			System.out.println("RS2 user client - release #" + 317);
			if(args.length != 5) {
				System.out.println("Usage: node-id, port-offset, [lowmem/highmem], [free/members], storeid");
				return;
			}
			nodeID = Integer.parseInt(args[0]);
			portOff = Integer.parseInt(args[1]);
			if(args[2].equals("lowmem"))
				setLowMem();
			else
				if(args[2].equals("highmem")) {
					setHighMem();
				} else {
					System.out.println("Usage: node-id, port-offset, [lowmem/highmem], [free/members], storeid");
					return;
				}
			if(args[3].equals("free"))
				isMembers = false;
			else
				if(args[3].equals("members")) {
					isMembers = true;
				} else {
					System.out.println("Usage: node-id, port-offset, [lowmem/highmem], [free/members], storeid");
					return;
				}
			signlink.storeid = Integer.parseInt(args[4]);
			signlink.startpriv(InetAddress.getLocalHost());
			instance = new Client();
			instance.createClientFrame(765, 503);
		}
		catch(Exception exception)
		{
		}
	}

	private void loadingStages()
	{
		if(lowMem && loadingStage == 2 && MapRegion.anInt131 != floor_level)
		{
			gameArea.initDrawingArea();
			displayLoadingProgress("Loading - please wait.");
			gameArea.drawGraphics(getGameAreaX(), getGameAreaY(), super.graphics);
			loadingStage = 1;
			aLong824 = System.currentTimeMillis();
		}
		if(loadingStage == 1)
		{
			int j = method54();
			if(j != 0 && System.currentTimeMillis() - aLong824 > 0x57e40L) {
				signlink.reportError(getUsername() + " glcfb " + aLong1215 + "," + j + "," + lowMem + "," + jagCache[0] + "," + onDemandFetcher.getNodeCount() + "," + floor_level + "," + anInt1069 + "," + anInt1070);
				aLong824 = System.currentTimeMillis();
			}
		}
		if(loadingStage == 2 && floor_level != anInt985)
		{
			anInt985 = floor_level;
			method24(floor_level);
		}
	}

	private int method54()
	{
		for(int i = 0; i < aByteArrayArray1183.length; i++)
		{
			if(aByteArrayArray1183[i] == null && anIntArray1235[i] != -1)
				return -1;
			if(aByteArrayArray1247[i] == null && anIntArray1236[i] != -1)
				return -2;
		}

		boolean flag = true;
		for(int j = 0; j < aByteArrayArray1183.length; j++)
		{
			byte abyte0[] = aByteArrayArray1247[j];
			if(abyte0 != null)
			{
				int k = (anIntArray1234[j] >> 8) * 64 - baseX;
				int l = (anIntArray1234[j] & 0xff) * 64 - baseY;
				if(aBoolean1159)
				{
					k = 10;
					l = 10;
				}
				flag &= MapRegion.method189(k, abyte0, l);
			}
		}

		if(!flag)
			return -3;
		if(aBoolean1080)
		{
			return -4;
		} else
		{
			loadingStage = 2;
			MapRegion.anInt131 = floor_level;
			method22();
			out.putOpCode(121);
			return 0;
		}
	}

	private void method55()
	{
		for(Projectile projectile = (Projectile)aClass19_1013.head(); projectile != null; projectile = (Projectile)aClass19_1013.next())
			if(projectile.plane != floor_level || currentTime > projectile.speedTime)
				projectile.remove();
			else
				if(currentTime >= projectile.delayTime)
				{
					if(projectile.lockOn > 0)
					{
						NPC npc = npcArray[projectile.lockOn - 1];
						if(npc != null && npc.currentX >= 0 && npc.currentX < 13312 && npc.currentY >= 0 && npc.currentY < 13312)
							projectile.method455(currentTime, npc.currentY, method42(projectile.plane, npc.currentY, npc.currentX) - projectile.endHeight, npc.currentX);
					}
					if(projectile.lockOn < 0)
					{
						int j = -projectile.lockOn - 1;
						Player player;
						if(j == unknownInt10)
							player = myPlayer;
						else
							player = playerArray[j];
						if(player != null && player.currentX >= 0 && player.currentX < 13312 && player.currentY >= 0 && player.currentY < 13312)
							projectile.method455(currentTime, player.currentY, method42(projectile.plane, player.currentY, player.currentX) - projectile.endHeight, player.currentX);
					}
					projectile.method456(anInt945);
					worldController.method285(floor_level, projectile.anInt1595, (int)projectile.aDouble1587, -1, (int)projectile.aDouble1586, 60, (int)projectile.aDouble1585, projectile, false);
				}

	}

	public AppletContext getAppletContext()
	{
		if(signlink.mainapp != null)
			return signlink.mainapp.getAppletContext();
		else
			return super.getAppletContext();
	}

	@SuppressWarnings("unused")
	private void drawLogo()
	{
		byte abyte0[] = titleArchive.getData("title.dat");
		RSImage sprite = new RSImage(abyte0, this);
		leftFlames.initDrawingArea();
		sprite.drawInverse(0, 0);
		rightFlames.initDrawingArea();
		sprite.drawInverse(-637, 0);
		aRSImageProducer_1107.initDrawingArea();
		sprite.drawInverse(-128, 0);
		aRSImageProducer_1108.initDrawingArea();
		sprite.drawInverse(-202, -371);
		title.initDrawingArea();
		sprite.drawInverse(-202, -171);
		aRSImageProducer_1112.initDrawingArea();
		sprite.drawInverse(0, -265);
		aRSImageProducer_1113.initDrawingArea();
		sprite.drawInverse(-562, -265);
		aRSImageProducer_1114.initDrawingArea();
		sprite.drawInverse(-128, -171);
		aRSImageProducer_1115.initDrawingArea();
		sprite.drawInverse(-562, -171);
		int ai[] = new int[sprite.myWidth];
		for(int j = 0; j < sprite.myHeight; j++)
		{
			for(int k = 0; k < sprite.myWidth; k++)
				ai[k] = sprite.myPixels[(sprite.myWidth - k - 1) + sprite.myWidth * j];

			System.arraycopy(ai, 0, sprite.myPixels, sprite.myWidth * j, sprite.myWidth);

		}

		leftFlames.initDrawingArea();
		sprite.drawInverse(382, 0);
		rightFlames.initDrawingArea();
		sprite.drawInverse(-255, 0);
		aRSImageProducer_1107.initDrawingArea();
		sprite.drawInverse(254, 0);
		aRSImageProducer_1108.initDrawingArea();
		sprite.drawInverse(180, -371);
		title.initDrawingArea();
		sprite.drawInverse(180, -171);
		aRSImageProducer_1112.initDrawingArea();
		sprite.drawInverse(382, -265);
		aRSImageProducer_1113.initDrawingArea();
		sprite.drawInverse(-180, -265);
		aRSImageProducer_1114.initDrawingArea();
		sprite.drawInverse(254, -171);
		aRSImageProducer_1115.initDrawingArea();
		sprite.drawInverse(-180, -171);
		sprite = new RSImage(titleArchive, "logo", 0);
		aRSImageProducer_1107.initDrawingArea();
		sprite.drawImage(382 - sprite.myWidth / 2 - 128, 18);
		sprite = null;
		System.gc();

	}

	private void processOnDemandQueue()
	{
		do
		{
			Resource onDemandData;
			do
			{
				onDemandData = onDemandFetcher.getNextNode();
				if(onDemandData == null)
					return;
				if(onDemandData.type == 0)
				{
					Model.method460(onDemandData.data, onDemandData.id);
					if((onDemandFetcher.getModelIndex(onDemandData.id) & 0x62) != 0)
					{
						redrawTabArea = true;
						if(backDialogID != -1)
							inputTaken = true;
					}
				}
				if(onDemandData.type == 1 && onDemandData.data != null)
					FrameHeader.method529(onDemandData.data);
				if(onDemandData.type == 2 && onDemandData.id == nextSong && onDemandData.data != null)
					saveMidi(songChanging, onDemandData.data);
				if(onDemandData.type == 3 && loadingStage == 1)
				{
					for(int i = 0; i < aByteArrayArray1183.length; i++)
					{
						if(anIntArray1235[i] == onDemandData.id)
						{
							aByteArrayArray1183[i] = onDemandData.data;
							if(onDemandData.data == null)
								anIntArray1235[i] = -1;
							break;
						}
						if(anIntArray1236[i] != onDemandData.id)
							continue;
						aByteArrayArray1247[i] = onDemandData.data;
						if(onDemandData.data == null)
							anIntArray1236[i] = -1;
						break;
					}

				}
			} while(onDemandData.type != 93 || !onDemandFetcher.method564(onDemandData.id));
			MapRegion.prefetchObjects(new ByteBuffer(onDemandData.data), onDemandFetcher);
		} while(true);
	}

	private void calcFlamesPosition()
	{
		char c = '\u0100';
		for(int j = 10; j < 117; j++)
		{
			int k = (int)(Math.random() * 100D);
			if(k < 50)
				anIntArray828[j + (c - 2 << 7)] = 255;
		}
		for(int l = 0; l < 100; l++)
		{
			int i1 = (int)(Math.random() * 124D) + 2;
			int k1 = (int)(Math.random() * 128D) + 128;
			int k2 = i1 + (k1 << 7);
			anIntArray828[k2] = 192;
		}

		for(int j1 = 1; j1 < c - 1; j1++)
		{
			for(int l1 = 1; l1 < 127; l1++)
			{
				int l2 = l1 + (j1 << 7);
				anIntArray829[l2] = (anIntArray828[l2 - 1] + anIntArray828[l2 + 1] + anIntArray828[l2 - 128] + anIntArray828[l2 + 128]) / 4;
			}

		}

		anInt1275 += 128;
		if(anInt1275 > anIntArray1190.length)
		{
			anInt1275 -= anIntArray1190.length;
			int i2 = (int)(Math.random() * 12D);
			randomizeBackground(aBackgroundArray1152s[i2]);
		}
		for(int j2 = 1; j2 < c - 1; j2++)
		{
			for(int i3 = 1; i3 < 127; i3++)
			{
				int k3 = i3 + (j2 << 7);
				int i4 = anIntArray829[k3 + 128] - anIntArray1190[k3 + anInt1275 & anIntArray1190.length - 1] / 5;
				if(i4 < 0)
					i4 = 0;
				anIntArray828[k3] = i4;
			}

		}

		System.arraycopy(anIntArray969, 1, anIntArray969, 0, c - 1);

		anIntArray969[c - 1] = (int)(Math.sin((double)currentTime / 14D) * 16D + Math.sin((double)currentTime / 15D) * 14D + Math.sin((double)currentTime / 16D) * 12D);
		if(anInt1040 > 0)
			anInt1040 -= 4;
		if(anInt1041 > 0)
			anInt1041 -= 4;
		if(anInt1040 == 0 && anInt1041 == 0)
		{
			int l3 = (int)(Math.random() * 2000D);
			if(l3 == 0)
				anInt1040 = 1024;
			if(l3 == 1)
				anInt1041 = 1024;
		}
	}

	private boolean saveWave(byte abyte0[], int i)
	{
		return abyte0 == null || signlink.wavesave(abyte0, i);
	}

	private void method60(int id) {
		RSInterface rsi = RSInterface.cache[id];
		for(int index = 0; index < rsi.children.length; index++) {
			if(rsi.children[index] == -1) {
				break;
			}
			RSInterface child = RSInterface.cache[rsi.children[index]];
			if(child.type == 1) {
				method60(child.id);
			}
			child.currentFrame = 0;
			child.framesLeft = 0;
		}
	}

	private void drawHeadIcon() {
		if(anInt855 != 2)
			return;
		calcEntityScreenPos((anInt934 - baseX << 7) + anInt937, anInt936 * 2, (anInt935 - baseY << 7) + anInt938);
		if(spriteDrawX > -1 && currentTime % 20 < 10)
			headIcons[2].drawImage(spriteDrawX - 12, spriteDrawY - 28);
	}

	private void mainGameProcessor() {
		if (!isFixed()) {
			if (clientWidth != getResizableWidth()) {
				clientWidth = getResizableWidth();
				setClientDimensions(getClientWidth(), getClientHeight());
				updateGameArea();
			}
			if (clientHeight != getResizableHeight()) {
				clientHeight = getResizableHeight();
				setClientDimensions(getClientWidth(), getClientHeight());
				updateGameArea();
			}
		}
		if(anInt1104 > 1)
			anInt1104--;
		if(anInt1011 > 0)
			anInt1011--;
		for(int index = 0; index < 5; index++) {
			if(!parsePacket()) {
				break;
			}
		}
		if(!loggedIn)
			return;
		synchronized(mouseDetection.syncObject)
		{
			if(flagged)
			{
				if(super.clickMode3 != 0 || mouseDetection.coordsIndex >= 40)
				{
					out.putOpCode(45);
					out.writeWordBigEndian(0);
					int j2 = out.offset;
					int j3 = 0;
					for(int j4 = 0; j4 < mouseDetection.coordsIndex; j4++)
					{
						if(j2 - out.offset >= 240)
							break;
						j3++;
						int l4 = mouseDetection.coordsY[j4];
						if(l4 < 0)
							l4 = 0;
						else
							if(l4 > 502)
								l4 = 502;
						int k5 = mouseDetection.coordsX[j4];
						if(k5 < 0)
							k5 = 0;
						else
							if(k5 > 764)
								k5 = 764;
						int i6 = l4 * 765 + k5;
						if(mouseDetection.coordsY[j4] == -1 && mouseDetection.coordsX[j4] == -1)
						{
							k5 = -1;
							l4 = -1;
							i6 = 0x7ffff;
						}
						if(k5 == anInt1237 && l4 == anInt1238)
						{
							if(anInt1022 < 2047)
								anInt1022++;
						} else
						{
							int j6 = k5 - anInt1237;
							anInt1237 = k5;
							int k6 = l4 - anInt1238;
							anInt1238 = l4;
							if(anInt1022 < 8 && j6 >= -32 && j6 <= 31 && k6 >= -32 && k6 <= 31)
							{
								j6 += 32;
								k6 += 32;
								out.putShort((anInt1022 << 12) + (j6 << 6) + k6);
								anInt1022 = 0;
							} else
								if(anInt1022 < 8)
								{
									out.writeDWordBigEndian(0x800000 + (anInt1022 << 19) + i6);
									anInt1022 = 0;
								} else
								{
									out.putInt(0xc0000000 + (anInt1022 << 19) + i6);
									anInt1022 = 0;
								}
						}
					}

					out.putBytes(out.offset - j2);
					if(j3 >= mouseDetection.coordsIndex)
					{
						mouseDetection.coordsIndex = 0;
					} else
					{
						mouseDetection.coordsIndex -= j3;
						for(int i5 = 0; i5 < mouseDetection.coordsIndex; i5++)
						{
							mouseDetection.coordsX[i5] = mouseDetection.coordsX[i5 + j3];
							mouseDetection.coordsY[i5] = mouseDetection.coordsY[i5 + j3];
						}

					}
				}
			} else
			{
				mouseDetection.coordsIndex = 0;
			}
		}
		if(super.clickMode3 != 0)
		{
			long l = (super.aLong29 - aLong1220) / 50L;
			if(l > 4095L)
				l = 4095L;
			aLong1220 = super.aLong29;
			int k2 = super.saveClickY;
			if(k2 < 0)
				k2 = 0;
			else
				if(k2 > 502)
					k2 = 502;
			int k3 = super.saveClickX;
			if(k3 < 0)
				k3 = 0;
			else
				if(k3 > 764)
					k3 = 764;
			int k4 = k2 * 765 + k3;
			int j5 = 0;
			if(super.clickMode3 == 2)
				j5 = 1;
			int l5 = (int)l;
			out.putOpCode(241);
			out.putInt((l5 << 20) + (j5 << 19) + k4);
		}
		if(anInt1016 > 0)
			anInt1016--;
		if(super.keyArray[1] == 1 || super.keyArray[2] == 1 || super.keyArray[3] == 1 || super.keyArray[4] == 1)
			aBoolean1017 = true;
		if(aBoolean1017 && anInt1016 <= 0)
		{
			anInt1016 = 20;
			aBoolean1017 = false;
			out.putOpCode(86);
			out.putShort(anInt1184);
			out.method432(minimapInt1);
		}
		if(super.awtFocus && !aBoolean954)
		{
			aBoolean954 = true;
			out.putOpCode(3);
			out.writeWordBigEndian(1);
		}
		if(!super.awtFocus && aBoolean954)
		{
			aBoolean954 = false;
			out.putOpCode(3);
			out.writeWordBigEndian(0);
		}
		loadingStages();
		method115();
		method90();
		anInt1009++;
		if(anInt1009 > 750)
			dropClient();
		method114();
		method95();
		method38();
		anInt945++;
		if(crossType != 0)
		{
			crossIndex += 20;
			if(crossIndex >= 400)
				crossType = 0;
		}
		if(atInventoryInterfaceType != 0)
		{
			atInventoryLoopCycle++;
			if(atInventoryLoopCycle >= 15)
			{
				if(atInventoryInterfaceType == 2)
					redrawTabArea = true;
				if(atInventoryInterfaceType == 3)
					inputTaken = true;
				atInventoryInterfaceType = 0;
			}
		}
		if(activeInterfaceType != 0)
		{
			anInt989++;
			if(super.mouseX > anInt1087 + 5 || super.mouseX < anInt1087 - 5 || super.mouseY > anInt1088 + 5 || super.mouseY < anInt1088 - 5)
				aBoolean1242 = true;
			if(super.clickMode2 == 0)
			{
				if(activeInterfaceType == 2)
					redrawTabArea = true;
				if(activeInterfaceType == 3)
					inputTaken = true;
				activeInterfaceType = 0;
				if(aBoolean1242 && anInt989 >= 5)
				{
					lastActiveInvInterface = -1;
					processRightClick();
					if(lastActiveInvInterface == anInt1084 && mouseInvInterfaceIndex != anInt1085)
					{
						RSInterface class9 = RSInterface.cache[anInt1084];
						int j1 = 0;
						if(anInt913 == 1 && class9.contentType == 206)
							j1 = 1;
						if(class9.inventory[mouseInvInterfaceIndex] <= 0)
							j1 = 0;
						if(class9.deletesTargetSlot)
						{
							int l2 = anInt1085;
							int l3 = mouseInvInterfaceIndex;
							class9.inventory[l3] = class9.inventory[l2];
							class9.inventoryAmount[l3] = class9.inventoryAmount[l2];
							class9.inventory[l2] = -1;
							class9.inventoryAmount[l2] = 0;
						} else
							if(j1 == 1)
							{
								int i3 = anInt1085;
								for(int i4 = mouseInvInterfaceIndex; i3 != i4;)
									if(i3 > i4)
									{
										class9.swapInventoryItems(i3, i3 - 1);
										i3--;
									} else
										if(i3 < i4)
										{
											class9.swapInventoryItems(i3, i3 + 1);
											i3++;
										}

							} else
							{
								class9.swapInventoryItems(anInt1085, mouseInvInterfaceIndex);
							}
						out.putOpCode(214);
						out.method433(anInt1084);
						out.method424(j1);
						out.method433(anInt1085);
						out.method431(mouseInvInterfaceIndex);
					}
				} else
					if((anInt1253 == 1 || menuHasAddFriend(menuActionRow - 1)) && menuActionRow > 2)
						determineMenuSize();
					else
						if(menuActionRow > 0)
							doAction(menuActionRow - 1);
				atInventoryLoopCycle = 10;
				super.clickMode3 = 0;
			}
		}
		if(SceneGraph.clickTileX != -1) {
			int x = SceneGraph.clickTileX;
			int y = SceneGraph.clickTileY;
			boolean flag = doWalkTo(0, 0, 0, 0, myPlayer.pathY[0], 0, 0, y, myPlayer.pathX[0], true, x);
			SceneGraph.clickTileX = -1;
			if(flag) {
				crossX = super.saveClickX;
				crossY = super.saveClickY;
				crossType = 1;
				crossIndex = 0;
			}
		}
		if(super.clickMode3 == 1 && aString844 != null) {
			aString844 = null;
			inputTaken = true;
			super.clickMode3 = 0;
		}
		processMenuClick();
		processMainScreenClick();
		processTabClick();
		processChatModeClick();
		if(super.clickMode2 == 1 || super.clickMode3 == 1) {
			anInt1213++;
		}
		if (anInt1500 != 0 || anInt1044 != 0 || anInt1129 != 0) {
            if (anInt1501 < 50) {
                anInt1501++;
                if (anInt1501 == 50) {
                    if (anInt1500 != 0) {
                        inputTaken = true;
                    }
                    if (anInt1044 != 0) {
                        redrawTabArea = true;
                    }
                }
            }
        } else if (anInt1501 > 0) {
            anInt1501--;
        }
		if(loadingStage == 2)
			method108();
		if(loadingStage == 2 && aBoolean1160)
			calcCameraPos();
		for(int i1 = 0; i1 < 5; i1++)
			anIntArray1030[i1]++;

		method73();
		super.idleTime++;
		if (Constants.IDLE_LOGOUT_TIME != -1) {
			if(super.idleTime > Constants.IDLE_LOGOUT_TIME) {
				anInt1011 = 250;
				super.idleTime -= 500;
				out.putOpCode(PacketConstants.getSent().IDLE_LOGOUT);
			}
		}
		anInt988++;
		if(anInt988 > 500)
		{
			anInt988 = 0;
			int l1 = (int)(Math.random() * 8D);
			if((l1 & 1) == 1)
				anInt1278 += anInt1279;
			if((l1 & 2) == 2)
				anInt1131 += anInt1132;
			if((l1 & 4) == 4)
				anInt896 += anInt897;
		}
		if(anInt1278 < -50)
			anInt1279 = 2;
		if(anInt1278 > 50)
			anInt1279 = -2;
		if(anInt1131 < -55)
			anInt1132 = 2;
		if(anInt1131 > 55)
			anInt1132 = -2;
		if(anInt896 < -40)
			anInt897 = 1;
		if(anInt896 > 40)
			anInt897 = -1;
		anInt1254++;
		if(anInt1254 > 500) {
			anInt1254 = 0;
			int i2 = (int)(Math.random() * 8D);
			if((i2 & 1) == 1)
				minimapInt2 += anInt1210;
			if((i2 & 2) == 2)
				minimapInt3 += anInt1171;
		}
		if(minimapInt2 < -60)
			anInt1210 = 2;
		if(minimapInt2 > 60)
			anInt1210 = -2;
		if(minimapInt3 < -20)
			anInt1171 = 1;
		if(minimapInt3 > 10)
			anInt1171 = -1;
		anInt1010++;
		if(anInt1010 > 50) {
			out.putOpCode(0);
		}
		try {
			if(socketStream != null && out.offset > 0) {
				socketStream.queueBytes(out.offset, out.buffer);
				out.offset = 0;
				anInt1010 = 0;
			}
		} catch(IOException _ex) {
			dropClient();
		} catch(Exception exception) {
			resetLogout();
		}
	}

	private void method63()
	{
		GameObjectSpawnRequest class30_sub1 = (GameObjectSpawnRequest)deque.head();
		for(; class30_sub1 != null; class30_sub1 = (GameObjectSpawnRequest)deque.next())
			if(class30_sub1.anInt1294 == -1)
			{
				class30_sub1.anInt1302 = 0;
				method89(class30_sub1);
			} else
			{
				class30_sub1.remove();
			}

	}

	private void resetImageProducers()
	{
		if(aRSImageProducer_1107 != null)
			return;
		super.fullGameScreen = null;
		chatArea = null;
		mapArea = null;
		tabArea = null;
		gameArea = null;
		aRSImageProducer_1123 = null;
		aRSImageProducer_1124 = null;
		aRSImageProducer_1125 = null;
		leftFlames = new RSImageProducer(128, 265, getGameComponent());
		RSDrawingArea.setAllPixelsToZero();
		rightFlames = new RSImageProducer(128, 265, getGameComponent());
		RSDrawingArea.setAllPixelsToZero();
		aRSImageProducer_1107 = new RSImageProducer(509, 171, getGameComponent());
		RSDrawingArea.setAllPixelsToZero();
		aRSImageProducer_1108 = new RSImageProducer(360, 132, getGameComponent());
		RSDrawingArea.setAllPixelsToZero();
		//title = new RSImageProducer(360, 200, getGameComponent());
		title = new RSImageProducer(getClientWidth(), getClientHeight(), getGameComponent());
		RSDrawingArea.setAllPixelsToZero();
		aRSImageProducer_1112 = new RSImageProducer(202, 238, getGameComponent());
		RSDrawingArea.setAllPixelsToZero();
		aRSImageProducer_1113 = new RSImageProducer(203, 238, getGameComponent());
		RSDrawingArea.setAllPixelsToZero();
		aRSImageProducer_1114 = new RSImageProducer(74, 94, getGameComponent());
		RSDrawingArea.setAllPixelsToZero();
		aRSImageProducer_1115 = new RSImageProducer(75, 94, getGameComponent());
		RSDrawingArea.setAllPixelsToZero();
		if(titleArchive != null)
		{
			//drawLogo();
			loadTitleScreen();
		}
		welcomeScreenRaised = true;
	}

	public void displayProgress(String text, int percent) {
		int width = 300;
		int height = 35;
		int x = (getClientWidth() / 2) - (width / 2);
		int y = (getClientHeight() / 2) + 185;
		anInt1079 = percent;
		aString1049 = text;
		resetImageProducers();
		if(titleArchive == null) {
			super.displayProgress(text, percent);
			return;
		}
		title.initDrawingArea();
		background[0].drawImage((getClientWidth() / 2) - 382, (getClientHeight() / 2) - 251);
		background[1].drawImage(getClientWidth() / 2 + 1, (getClientHeight() / 2) - 251);
		background[2].drawImage((getClientWidth() / 2) - 382, getClientHeight() / 2 + 1);
		background[3].drawImage(getClientWidth() / 2 + 1, getClientHeight() / 2 + 1);
		RSDrawingArea.drawRoundedRectangle(x, y, width, height, 0x0F0200, 230, true, false);
		RSDrawingArea.drawRoundedRectangle(x + 2, y + 2, percent * 3 - 4, height - 4, 0x570000, 100, true, false);
		fancy.drawCenteredString(text + " (" + percent + "%)", getClientWidth() / 2, y + (height / 2) + 7, 0xFFFFFF, true);
		title.drawGraphics(0, 0, super.graphics);
		if(welcomeScreenRaised) {
			welcomeScreenRaised = false;
		}
	}

	private void method65(int i, int j, int k, int l, RSInterface class9, int i1, boolean flag,
			int j1)
	{
		int anInt992;
		if(aBoolean972)
			anInt992 = 32;
		else
			anInt992 = 0;
		aBoolean972 = false;
		if(k >= i && k < i + 16 && l >= i1 && l < i1 + 16)
		{
			class9.scrollPosition -= anInt1213 * 4;
			if(flag)
			{
				redrawTabArea = true;
			}
		} else
			if(k >= i && k < i + 16 && l >= (i1 + j) - 16 && l < i1 + j)
			{
				class9.scrollPosition += anInt1213 * 4;
				if(flag)
				{
					redrawTabArea = true;
				}
			} else
				if(k >= i - anInt992 && k < i + 16 + anInt992 && l >= i1 + 16 && l < (i1 + j) - 16 && anInt1213 > 0)
				{
					int l1 = ((j - 32) * j) / j1;
					if(l1 < 8)
						l1 = 8;
					int i2 = l - i1 - 16 - l1 / 2;
					int j2 = j - 32 - l1;
					class9.scrollPosition = ((j1 - j) * i2) / j2;
					if(flag)
						redrawTabArea = true;
					aBoolean972 = true;
				}
	}

	private boolean method66(int i, int j, int k)
	{
		int i1 = i >> 14 & 0x7fff;
					int j1 = worldController.getIdTagForPosition(floor_level, k, j, i);
					if(j1 == -1)
						return false;
					int k1 = j1 & 0x1f;
					int l1 = j1 >> 6 & 3;
								if(k1 == 10 || k1 == 11 || k1 == 22)
								{
									ObjectDef class46 = ObjectDef.getDef(i1);
									int i2;
									int j2;
									if(l1 == 0 || l1 == 2)
									{
										i2 = class46.sizeX;
										j2 = class46.sizeY;
									} else
									{
										i2 = class46.sizeY;
										j2 = class46.sizeX;
									}
									int k2 = class46.anInt768;
									if(l1 != 0)
										k2 = (k2 << l1 & 0xf) + (k2 >> 4 - l1);
									doWalkTo(2, 0, j2, 0, myPlayer.pathY[0], i2, k2, j, myPlayer.pathX[0], false, k);
								} else
								{
									doWalkTo(2, l1, 0, k1 + 1, myPlayer.pathY[0], 0, 0, j, myPlayer.pathX[0], false, k);
								}
								crossX = super.saveClickX;
								crossY = super.saveClickY;
								crossType = 2;
								crossIndex = 0;
								return true;
	}

	private JagexArchive streamLoaderForName(int i, String s, String s1, int j, int k)
	{
		byte abyte0[] = null;
		int l = 5;
		try
		{
			if(jagCache[0] != null)
				abyte0 = jagCache[0].get(i);
		}
		catch(Exception _ex) { }
		if(abyte0 != null)
		{
			//		aCRC32_930.reset();
			//		aCRC32_930.update(abyte0);
			//		int i1 = (int)aCRC32_930.getValue();
			//		if(i1 != j)
		}
		if(abyte0 != null)
		{
			JagexArchive streamLoader = new JagexArchive(abyte0);
			return streamLoader;
		}
		int j1 = 0;
		while(abyte0 == null)
		{
			String s2 = "Unknown error";
			displayProgress("Requesting " + s, k);
			try
			{
				int k1 = 0;
				DataInputStream datainputstream = openJagGrabInputStream(s1 + j);
				byte abyte1[] = new byte[6];
				datainputstream.readFully(abyte1, 0, 6);
				ByteBuffer stream = new ByteBuffer(abyte1);
				stream.offset = 3;
				int i2 = stream.get3Bytes() + 6;
				int j2 = 6;
				abyte0 = new byte[i2];
				System.arraycopy(abyte1, 0, abyte0, 0, 6);

				while(j2 < i2) 
				{
					int l2 = i2 - j2;
					if(l2 > 1000)
						l2 = 1000;
					int j3 = datainputstream.read(abyte0, j2, l2);
					if(j3 < 0)
					{
						s2 = "Length error: " + j2 + "/" + i2;
						throw new IOException("EOF");
					}
					j2 += j3;
					int k3 = (j2 * 100) / i2;
					if(k3 != k1)
						displayProgress("Loading " + s + " - " + k3 + "%", k);
					k1 = k3;
				}
				datainputstream.close();
				try
				{
					if(jagCache[0] != null)
						jagCache[0].put(abyte0.length, abyte0, i);
				}
				catch(Exception _ex)
				{
					jagCache[0] = null;
				}
				/*			 if(abyte0 != null)
				{
					aCRC32_930.reset();
					aCRC32_930.update(abyte0);
					int i3 = (int)aCRC32_930.getValue();
					if(i3 != j)
					{
						abyte0 = null;
						j1++;
						s2 = "Checksum error: " + i3;
					}
				}
				 */
			}
			catch(IOException ioexception)
			{
				if(s2.equals("Unknown error"))
					s2 = "Connection error";
				abyte0 = null;
			}
			catch(NullPointerException _ex)
			{
				s2 = "Null error";
				abyte0 = null;
				if(!signlink.reporterror)
					return null;
			}
			catch(ArrayIndexOutOfBoundsException _ex)
			{
				s2 = "Bounds error";
				abyte0 = null;
				if(!signlink.reporterror)
					return null;
			}
			catch(Exception _ex)
			{
				s2 = "Unexpected error";
				abyte0 = null;
				if(!signlink.reporterror)
					return null;
			}
			if(abyte0 == null)
			{
				for(int l1 = l; l1 > 0; l1--)
				{
					if(j1 >= 3)
					{
						displayProgress("Game updated - please reload page", k);
						l1 = 10;
					} else
					{
						displayProgress(s2 + " - Retrying in " + l1, k);
					}
					try
					{
						Thread.sleep(1000L);
					}
					catch(Exception _ex) { }
				}

				l *= 2;
				if(l > 60)
					l = 60;
				aBoolean872 = !aBoolean872;
			}

		}

		JagexArchive streamLoader_1 = new JagexArchive(abyte0);
		return streamLoader_1;
	}

	private void dropClient()
	{
		if(anInt1011 > 0)
		{
			resetLogout();
			return;
		}
		gameArea.initDrawingArea();
		displayLoadingProgress("Connection lost\\nPlease wait - attempting to reestablish");
		gameArea.drawGraphics(getGameAreaX(), getGameAreaY(), super.graphics);
		anInt1021 = 0;
		destX = 0;
		RSSocket rsSocket = socketStream;
		loggedIn = false;
		loginFailures = 0;
		login(getUsername(), myPassword, true, false);
		if(!loggedIn)
			resetLogout();
		try
		{
			rsSocket.close();
		}
		catch(Exception _ex)
		{
		}
	}

	private void doAction(int actionIndex) {
		if(actionIndex < 0) {
			return;
		}
		if(dialogState != 0) {
			dialogState = 0;
			inputTaken = true;
		}
		int cmd1 = menuActionCmd1[actionIndex];
		int cmd2 = menuActionCmd2[actionIndex];
		int cmd3 = menuActionCmd3[actionIndex];
		int action = menuActionID[actionIndex];
		if(action >= 2000) {
			action -= 2000;
		}
		switch (action) {
			case VIEW_ALL:
				setCurrentChatMode(ALL);
				break;
			case VIEW_GAME:
				setCurrentChatMode(GAME);
				break;
			case VIEW_PUBLIC:
				setCurrentChatMode(PUBLIC);
				break;
			case ON_PUBLIC:
				publicChatMode = 0;
				sendChatModes();
				break;
			case FRIENDS_PUBLIC:
				publicChatMode = 1;
				sendChatModes();
				break;
			case OFF_PUBLIC:
				publicChatMode = 2;
				sendChatModes();
				break;
			case HIDE_PUBLIC:
				publicChatMode = 3;
				sendChatModes();
				sendChatModes();
				break;
			case VIEW_PRIVATE:
				setCurrentChatMode(PRIVATE);
				break;
			case ON_PRIVATE:
				privateChatMode = 0;
				sendChatModes();
				break;
			case FRIENDS_PRIVATE:
				privateChatMode = 1;
				sendChatModes();
				break;
			case OFF_PRIVATE:
				privateChatMode = 2;
				sendChatModes();
				break;
			case VIEW_CLAN:
				setCurrentChatMode(CLAN);
				break;
			case ON_CLAN:
				clanChatMode = 0;
				sendChatModes();
				break;
			case FRIENDS_CLAN:
				clanChatMode = 1;
				sendChatModes();
				break;
			case OFF_CLAN:
				clanChatMode = 2;
				sendChatModes();
				break;
			case VIEW_YELL:
				setCurrentChatMode(YELL);
				break;
			case ON_YELL:
				yellChatMode = 0;
				sendChatModes();
				break;
			case FRIENDS_YELL:
				yellChatMode = 1;
				sendChatModes();
				break;
			case OFF_YELL:
				yellChatMode = 2;
				sendChatModes();
				break;
			case VIEW_TRADE:
				setCurrentChatMode(TRADE);
				break;
			case ON_TRADE:
				tradeMode = 0;
				sendChatModes();
				break;
			case FRIENDS_TRADE:
				tradeMode = 1;
				sendChatModes();
				break;
			case OFF_TRADE:
				tradeMode = 2;
				sendChatModes();
				break;
			case REPORT_ABUSE:
				if(openInterfaceID == -1) {
					clearTopInterfaces();
					reportAbuseInput = "";
					canMute = false;
					for(int index = 0; index < RSInterface.cache.length; index++) {
						if(RSInterface.cache[index] == null || RSInterface.cache[index].contentType != 600) {
							continue;
						}
						reportAbuseInterfaceID = openInterfaceID = RSInterface.cache[index].parentId;
						break;
					}
				} else {
					pushMessage("", "Please close the interface you have open before using 'report abuse'", 0);
				}
				break;
		}
		if(action == 582) {
			NPC npc = npcArray[cmd1];
			if(npc != null) {
				doWalkTo(2, 0, 1, 0, myPlayer.pathY[0], 1, 0, npc.pathY[0], myPlayer.pathX[0], false, npc.pathX[0]);
				crossX = super.saveClickX;
				crossY = super.saveClickY;
				crossType = 2;
				crossIndex = 0;
				out.putOpCode(57);
				out.method432(anInt1285);
				out.method432(cmd1);
				out.method431(anInt1283);
				out.method432(anInt1284);
			}
		}
		if(action == 234) {
			boolean flag1 = doWalkTo(2, 0, 0, 0, myPlayer.pathY[0], 0, 0, cmd3, myPlayer.pathX[0], false, cmd2);
			if(!flag1)
				flag1 = doWalkTo(2, 0, 1, 0, myPlayer.pathY[0], 1, 0, cmd3, myPlayer.pathX[0], false, cmd2);
			crossX = super.saveClickX;
			crossY = super.saveClickY;
			crossType = 2;
			crossIndex = 0;
			out.putOpCode(236);
			out.method431(cmd3 + baseY);
			out.putShort(cmd1);
			out.method431(cmd2 + baseX);
		}
		if(action == 62 && method66(cmd1, cmd3, cmd2))
		{
			out.putOpCode(192);
			out.putShort(anInt1284);
			out.method431(cmd1 >> 14 & 0x7fff);
			out.method433(cmd3 + baseY);
			out.method431(anInt1283);
			out.method433(cmd2 + baseX);
			out.putShort(anInt1285);
		}
		if(action == 511) {
			boolean flag2 = doWalkTo(2, 0, 0, 0, myPlayer.pathY[0], 0, 0, cmd3, myPlayer.pathX[0], false, cmd2);
			if(!flag2)
				flag2 = doWalkTo(2, 0, 1, 0, myPlayer.pathY[0], 1, 0, cmd3, myPlayer.pathX[0], false, cmd2);
			crossX = super.saveClickX;
			crossY = super.saveClickY;
			crossType = 2;
			crossIndex = 0;
			out.putOpCode(25);
			out.method431(anInt1284);
			out.method432(anInt1285);
			out.putShort(cmd1);
			out.method432(cmd3 + baseY);
			out.method433(anInt1283);
			out.putShort(cmd2 + baseX);
		}
		if(action == 74)
		{
			out.putOpCode(122);
			out.method433(cmd3);
			out.method432(cmd2);
			out.method431(cmd1);
			atInventoryLoopCycle = 0;
			atInventoryInterface = cmd3;
			atInventoryIndex = cmd2;
			atInventoryInterfaceType = 2;
			if(RSInterface.cache[cmd3].parentId == openInterfaceID)
				atInventoryInterfaceType = 1;
			if(RSInterface.cache[cmd3].parentId == backDialogID)
				atInventoryInterfaceType = 3;
		}
		if(action == 315)
		{
			RSInterface class9 = RSInterface.cache[cmd3];
			boolean flag8 = true;
			if(class9.contentType > 0)
				flag8 = promptUserForInput(class9);
			if(flag8) {
				out.putOpCode(185);
				out.putShort(cmd3);
			}
		}
		if(action == 561)
		{
			Player player = playerArray[cmd1];
			if(player != null)
			{
				doWalkTo(2, 0, 1, 0, myPlayer.pathY[0], 1, 0, player.pathY[0], myPlayer.pathX[0], false, player.pathX[0]);
				crossX = super.saveClickX;
				crossY = super.saveClickY;
				crossType = 2;
				crossIndex = 0;
				anInt1188 += cmd1;
				if(anInt1188 >= 90)
				{
					out.putOpCode(136);
					anInt1188 = 0;
				}
				out.putOpCode(128);
				out.putShort(cmd1);
			}
		}
		if(action == 20)
		{
			NPC class30_sub2_sub4_sub1_sub1_1 = npcArray[cmd1];
			if(class30_sub2_sub4_sub1_sub1_1 != null)
			{
				doWalkTo(2, 0, 1, 0, myPlayer.pathY[0], 1, 0, class30_sub2_sub4_sub1_sub1_1.pathY[0], myPlayer.pathX[0], false, class30_sub2_sub4_sub1_sub1_1.pathX[0]);
				crossX = super.saveClickX;
				crossY = super.saveClickY;
				crossType = 2;
				crossIndex = 0;
				out.putOpCode(155);
				out.method431(cmd1);
			}
		}
		if(action == 779)
		{
			Player class30_sub2_sub4_sub1_sub2_1 = playerArray[cmd1];
			if(class30_sub2_sub4_sub1_sub2_1 != null)
			{
				doWalkTo(2, 0, 1, 0, myPlayer.pathY[0], 1, 0, class30_sub2_sub4_sub1_sub2_1.pathY[0], myPlayer.pathX[0], false, class30_sub2_sub4_sub1_sub2_1.pathX[0]);
				crossX = super.saveClickX;
				crossY = super.saveClickY;
				crossType = 2;
				crossIndex = 0;
				out.putOpCode(153);
				out.method431(cmd1);
			}
		}
		if(action == 516)
			if(!menuOpen)
				worldController.request2DTrace(super.saveClickY - 4, super.saveClickX - 4);
			else
				worldController.request2DTrace(cmd3 - 4, cmd2 - 4);
		if(action == 1062)
		{
			anInt924 += baseX;
			if(anInt924 >= 113)
			{
				out.putOpCode(183);
				out.writeDWordBigEndian(0xe63271);
				anInt924 = 0;
			}
			method66(cmd1, cmd3, cmd2);
			out.putOpCode(228);
			out.method432(cmd1 >> 14 & 0x7fff);
			out.method432(cmd3 + baseY);
			out.putShort(cmd2 + baseX);
		}
		if(action == 679 && !aBoolean1149)
		{
			out.putOpCode(40);
			out.putShort(cmd3);
			aBoolean1149 = true;
		}
		if(action == 431)
		{
			out.putOpCode(129);
			out.method432(cmd2);
			out.putShort(cmd3);
			out.method432(cmd1);
			atInventoryLoopCycle = 0;
			atInventoryInterface = cmd3;
			atInventoryIndex = cmd2;
			atInventoryInterfaceType = 2;
			if(RSInterface.cache[cmd3].parentId == openInterfaceID)
				atInventoryInterfaceType = 1;
			if(RSInterface.cache[cmd3].parentId == backDialogID)
				atInventoryInterfaceType = 3;
		}
		if(action == 337 || action == 42 || action == 792 || action == 322)
		{
			String s = menuActionName[actionIndex];
			int k1 = s.indexOf("@whi@");
			if(k1 != -1)
			{
				long l3 = TextUtils.longForName(s.substring(k1 + 5).trim());
				if(action == 337)
					addFriend(l3);
				if(action == 42)
					addIgnore(l3);
				if(action == 792)
					delFriend(l3);
				if(action == 322)
					delIgnore(l3);
			}
		}
		if(action == 53)
		{
			out.putOpCode(135);
			out.method431(cmd2);
			out.method432(cmd3);
			out.method431(cmd1);
			atInventoryLoopCycle = 0;
			atInventoryInterface = cmd3;
			atInventoryIndex = cmd2;
			atInventoryInterfaceType = 2;
			if(RSInterface.cache[cmd3].parentId == openInterfaceID)
				atInventoryInterfaceType = 1;
			if(RSInterface.cache[cmd3].parentId == backDialogID)
				atInventoryInterfaceType = 3;
		}
		if(action == 539)
		{
			out.putOpCode(16);
			out.method432(cmd1);
			out.method433(cmd2);
			out.method433(cmd3);
			atInventoryLoopCycle = 0;
			atInventoryInterface = cmd3;
			atInventoryIndex = cmd2;
			atInventoryInterfaceType = 2;
			if(RSInterface.cache[cmd3].parentId == openInterfaceID)
				atInventoryInterfaceType = 1;
			if(RSInterface.cache[cmd3].parentId == backDialogID)
				atInventoryInterfaceType = 3;
		}
		if(action == 484 || action == 6)
		{
			String s1 = menuActionName[actionIndex];
			int l1 = s1.indexOf("@whi@");
			if(l1 != -1)
			{
				s1 = s1.substring(l1 + 5).trim();
				String s7 = TextUtils.fixName(TextUtils.nameForLong(TextUtils.longForName(s1)));
				boolean flag9 = false;
				for(int j3 = 0; j3 < playerCount; j3++)
				{
					Player class30_sub2_sub4_sub1_sub2_7 = playerArray[playerIndices[j3]];
					if(class30_sub2_sub4_sub1_sub2_7 == null || class30_sub2_sub4_sub1_sub2_7.name == null || !class30_sub2_sub4_sub1_sub2_7.name.equalsIgnoreCase(s7))
						continue;
					doWalkTo(2, 0, 1, 0, myPlayer.pathY[0], 1, 0, class30_sub2_sub4_sub1_sub2_7.pathY[0], myPlayer.pathX[0], false, class30_sub2_sub4_sub1_sub2_7.pathX[0]);
					if(action == 484)
					{
						out.putOpCode(139);
						out.method431(playerIndices[j3]);
					}
					if(action == 6)
					{
						anInt1188 += cmd1;
						if(anInt1188 >= 90)
						{
							out.putOpCode(136);
							anInt1188 = 0;
						}
						out.putOpCode(128);
						out.putShort(playerIndices[j3]);
					}
					flag9 = true;
					break;
				}

				if(!flag9)
					pushMessage("", "Unable to find " + s7, 0);
			}
		}
		if(action == 870)
		{
			out.putOpCode(53);
			out.putShort(cmd2);
			out.method432(anInt1283);
			out.method433(cmd1);
			out.putShort(anInt1284);
			out.method431(anInt1285);
			out.putShort(cmd3);
			atInventoryLoopCycle = 0;
			atInventoryInterface = cmd3;
			atInventoryIndex = cmd2;
			atInventoryInterfaceType = 2;
			if(RSInterface.cache[cmd3].parentId == openInterfaceID)
				atInventoryInterfaceType = 1;
			if(RSInterface.cache[cmd3].parentId == backDialogID)
				atInventoryInterfaceType = 3;
		}
		if(action == 847)
		{
			out.putOpCode(87);
			out.method432(cmd1);
			out.putShort(cmd3);
			out.method432(cmd2);
			atInventoryLoopCycle = 0;
			atInventoryInterface = cmd3;
			atInventoryIndex = cmd2;
			atInventoryInterfaceType = 2;
			if(RSInterface.cache[cmd3].parentId == openInterfaceID)
				atInventoryInterfaceType = 1;
			if(RSInterface.cache[cmd3].parentId == backDialogID)
				atInventoryInterfaceType = 3;
		}
		if(action == 626)
		{
			RSInterface class9_1 = RSInterface.cache[cmd3];
			spellSelected = 1;
			anInt1137 = cmd3;
			spellUsableOn = class9_1.spellUsableOn;
			itemSelected = 0;
			redrawTabArea = true;
			String s4 = class9_1.selectedActionName;
			if(s4.indexOf(" ") != -1)
				s4 = s4.substring(0, s4.indexOf(" "));
			String s8 = class9_1.selectedActionName;
			if(s8.indexOf(" ") != -1)
				s8 = s8.substring(s8.indexOf(" ") + 1);
			spellTooltip = s4 + " " + class9_1.spellName + " " + s8;
			if(spellUsableOn == 16)
			{
				redrawTabArea = true;
				tabID = 3;
				tabAreaAltered = true;
			}
			return;
		}
		if(action == 78)
		{
			out.putOpCode(117);
			out.method433(cmd3);
			out.method433(cmd1);
			out.method431(cmd2);
			atInventoryLoopCycle = 0;
			atInventoryInterface = cmd3;
			atInventoryIndex = cmd2;
			atInventoryInterfaceType = 2;
			if(RSInterface.cache[cmd3].parentId == openInterfaceID)
				atInventoryInterfaceType = 1;
			if(RSInterface.cache[cmd3].parentId == backDialogID)
				atInventoryInterfaceType = 3;
		}
		if(action == 27)
		{
			Player class30_sub2_sub4_sub1_sub2_2 = playerArray[cmd1];
			if(class30_sub2_sub4_sub1_sub2_2 != null)
			{
				doWalkTo(2, 0, 1, 0, myPlayer.pathY[0], 1, 0, class30_sub2_sub4_sub1_sub2_2.pathY[0], myPlayer.pathX[0], false, class30_sub2_sub4_sub1_sub2_2.pathX[0]);
				crossX = super.saveClickX;
				crossY = super.saveClickY;
				crossType = 2;
				crossIndex = 0;
				anInt986 += cmd1;
				if(anInt986 >= 54)
				{
					out.putOpCode(189);
					out.writeWordBigEndian(234);
					anInt986 = 0;
				}
				out.putOpCode(73);
				out.method431(cmd1);
			}
		}
		if(action == 213)
		{
			boolean flag3 = doWalkTo(2, 0, 0, 0, myPlayer.pathY[0], 0, 0, cmd3, myPlayer.pathX[0], false, cmd2);
			if(!flag3)
				flag3 = doWalkTo(2, 0, 1, 0, myPlayer.pathY[0], 1, 0, cmd3, myPlayer.pathX[0], false, cmd2);
			crossX = super.saveClickX;
			crossY = super.saveClickY;
			crossType = 2;
			crossIndex = 0;
			out.putOpCode(79);
			out.method431(cmd3 + baseY);
			out.putShort(cmd1);
			out.method432(cmd2 + baseX);
		}
		if(action == 632)
		{
			out.putOpCode(145);
			out.method432(cmd3);
			out.method432(cmd2);
			out.method432(cmd1);
			atInventoryLoopCycle = 0;
			atInventoryInterface = cmd3;
			atInventoryIndex = cmd2;
			atInventoryInterfaceType = 2;
			if(RSInterface.cache[cmd3].parentId == openInterfaceID)
				atInventoryInterfaceType = 1;
			if(RSInterface.cache[cmd3].parentId == backDialogID)
				atInventoryInterfaceType = 3;
		}
		if(action == 493)
		{
			out.putOpCode(75);
			out.method433(cmd3);
			out.method431(cmd2);
			out.method432(cmd1);
			atInventoryLoopCycle = 0;
			atInventoryInterface = cmd3;
			atInventoryIndex = cmd2;
			atInventoryInterfaceType = 2;
			if(RSInterface.cache[cmd3].parentId == openInterfaceID)
				atInventoryInterfaceType = 1;
			if(RSInterface.cache[cmd3].parentId == backDialogID)
				atInventoryInterfaceType = 3;
		}
		if(action == 652)
		{
			boolean flag4 = doWalkTo(2, 0, 0, 0, myPlayer.pathY[0], 0, 0, cmd3, myPlayer.pathX[0], false, cmd2);
			if(!flag4)
				flag4 = doWalkTo(2, 0, 1, 0, myPlayer.pathY[0], 1, 0, cmd3, myPlayer.pathX[0], false, cmd2);
			crossX = super.saveClickX;
			crossY = super.saveClickY;
			crossType = 2;
			crossIndex = 0;
			out.putOpCode(156);
			out.method432(cmd2 + baseX);
			out.method431(cmd3 + baseY);
			out.method433(cmd1);
		}
		if(action == 94)
		{
			boolean flag5 = doWalkTo(2, 0, 0, 0, myPlayer.pathY[0], 0, 0, cmd3, myPlayer.pathX[0], false, cmd2);
			if(!flag5)
				flag5 = doWalkTo(2, 0, 1, 0, myPlayer.pathY[0], 1, 0, cmd3, myPlayer.pathX[0], false, cmd2);
			crossX = super.saveClickX;
			crossY = super.saveClickY;
			crossType = 2;
			crossIndex = 0;
			out.putOpCode(181);
			out.method431(cmd3 + baseY);
			out.putShort(cmd1);
			out.method431(cmd2 + baseX);
			out.method432(anInt1137);
		}
		if(action == 646)
		{
			out.putOpCode(185);
			out.putShort(cmd3);
			RSInterface class9_2 = RSInterface.cache[cmd3];
			if(class9_2.valueIndexArray != null && class9_2.valueIndexArray[0][0] == 5)
			{
				int i2 = class9_2.valueIndexArray[0][1];
				if(variousSettings[i2] != class9_2.requiredValues[0])
				{
					variousSettings[i2] = class9_2.requiredValues[0];
					handleActions(i2);
					redrawTabArea = true;
				}
			}
		}
		if(action == 225)
		{
			NPC class30_sub2_sub4_sub1_sub1_2 = npcArray[cmd1];
			if(class30_sub2_sub4_sub1_sub1_2 != null)
			{
				doWalkTo(2, 0, 1, 0, myPlayer.pathY[0], 1, 0, class30_sub2_sub4_sub1_sub1_2.pathY[0], myPlayer.pathX[0], false, class30_sub2_sub4_sub1_sub1_2.pathX[0]);
				crossX = super.saveClickX;
				crossY = super.saveClickY;
				crossType = 2;
				crossIndex = 0;
				anInt1226 += cmd1;
				if(anInt1226 >= 85)
				{
					out.putOpCode(230);
					out.writeWordBigEndian(239);
					anInt1226 = 0;
				}
				out.putOpCode(17);
				out.method433(cmd1);
			}
		}
		if(action == 965)
		{
			NPC class30_sub2_sub4_sub1_sub1_3 = npcArray[cmd1];
			if(class30_sub2_sub4_sub1_sub1_3 != null)
			{
				doWalkTo(2, 0, 1, 0, myPlayer.pathY[0], 1, 0, class30_sub2_sub4_sub1_sub1_3.pathY[0], myPlayer.pathX[0], false, class30_sub2_sub4_sub1_sub1_3.pathX[0]);
				crossX = super.saveClickX;
				crossY = super.saveClickY;
				crossType = 2;
				crossIndex = 0;
				anInt1134++;
				if(anInt1134 >= 96)
				{
					out.putOpCode(152);
					out.writeWordBigEndian(88);
					anInt1134 = 0;
				}
				out.putOpCode(21);
				out.putShort(cmd1);
			}
		}
		if(action == 413)
		{
			NPC class30_sub2_sub4_sub1_sub1_4 = npcArray[cmd1];
			if(class30_sub2_sub4_sub1_sub1_4 != null)
			{
				doWalkTo(2, 0, 1, 0, myPlayer.pathY[0], 1, 0, class30_sub2_sub4_sub1_sub1_4.pathY[0], myPlayer.pathX[0], false, class30_sub2_sub4_sub1_sub1_4.pathX[0]);
				crossX = super.saveClickX;
				crossY = super.saveClickY;
				crossType = 2;
				crossIndex = 0;
				out.putOpCode(131);
				out.method433(cmd1);
				out.method432(anInt1137);
			}
		}
		if(action == 200)
			clearTopInterfaces();
		if(action == 1025)
		{
			NPC class30_sub2_sub4_sub1_sub1_5 = npcArray[cmd1];
			if(class30_sub2_sub4_sub1_sub1_5 != null)
			{
				NPCDef entityDef = class30_sub2_sub4_sub1_sub1_5.desc;
				if(entityDef.childrenIDs != null)
					entityDef = entityDef.method161();
				if(entityDef != null)
				{
					String s9;
					if(entityDef.description != null)
						s9 = new String(entityDef.description);
					else
						s9 = "It's a " + entityDef.name + ".";
					pushMessage("", s9, 0);
				}
			}
		}
		if(action == 900)
		{
			method66(cmd1, cmd3, cmd2);
			out.putOpCode(252);
			out.method433(cmd1 >> 14 & 0x7fff);
			out.method431(cmd3 + baseY);
			out.method432(cmd2 + baseX);
		}
		if(action == 412)
		{
			NPC class30_sub2_sub4_sub1_sub1_6 = npcArray[cmd1];
			if(class30_sub2_sub4_sub1_sub1_6 != null)
			{
				doWalkTo(2, 0, 1, 0, myPlayer.pathY[0], 1, 0, class30_sub2_sub4_sub1_sub1_6.pathY[0], myPlayer.pathX[0], false, class30_sub2_sub4_sub1_sub1_6.pathX[0]);
				crossX = super.saveClickX;
				crossY = super.saveClickY;
				crossType = 2;
				crossIndex = 0;
				out.putOpCode(72);
				out.method432(cmd1);
			}
		}
		if(action == 365)
		{
			Player class30_sub2_sub4_sub1_sub2_3 = playerArray[cmd1];
			if(class30_sub2_sub4_sub1_sub2_3 != null)
			{
				doWalkTo(2, 0, 1, 0, myPlayer.pathY[0], 1, 0, class30_sub2_sub4_sub1_sub2_3.pathY[0], myPlayer.pathX[0], false, class30_sub2_sub4_sub1_sub2_3.pathX[0]);
				crossX = super.saveClickX;
				crossY = super.saveClickY;
				crossType = 2;
				crossIndex = 0;
				out.putOpCode(249);
				out.method432(cmd1);
				out.method431(anInt1137);
			}
		}
		if(action == 729)
		{
			Player class30_sub2_sub4_sub1_sub2_4 = playerArray[cmd1];
			if(class30_sub2_sub4_sub1_sub2_4 != null)
			{
				doWalkTo(2, 0, 1, 0, myPlayer.pathY[0], 1, 0, class30_sub2_sub4_sub1_sub2_4.pathY[0], myPlayer.pathX[0], false, class30_sub2_sub4_sub1_sub2_4.pathX[0]);
				crossX = super.saveClickX;
				crossY = super.saveClickY;
				crossType = 2;
				crossIndex = 0;
				out.putOpCode(39);
				out.method431(cmd1);
			}
		}
		if(action == 577)
		{
			Player class30_sub2_sub4_sub1_sub2_5 = playerArray[cmd1];
			if(class30_sub2_sub4_sub1_sub2_5 != null)
			{
				doWalkTo(2, 0, 1, 0, myPlayer.pathY[0], 1, 0, class30_sub2_sub4_sub1_sub2_5.pathY[0], myPlayer.pathX[0], false, class30_sub2_sub4_sub1_sub2_5.pathX[0]);
				crossX = super.saveClickX;
				crossY = super.saveClickY;
				crossType = 2;
				crossIndex = 0;
				out.putOpCode(139);
				out.method431(cmd1);
			}
		}
		if(action == 956 && method66(cmd1, cmd3, cmd2))
		{
			out.putOpCode(35);
			out.method431(cmd2 + baseX);
			out.method432(anInt1137);
			out.method432(cmd3 + baseY);
			out.method431(cmd1 >> 14 & 0x7fff);
		}
		if(action == 567)
		{
			boolean flag6 = doWalkTo(2, 0, 0, 0, myPlayer.pathY[0], 0, 0, cmd3, myPlayer.pathX[0], false, cmd2);
			if(!flag6)
				flag6 = doWalkTo(2, 0, 1, 0, myPlayer.pathY[0], 1, 0, cmd3, myPlayer.pathX[0], false, cmd2);
			crossX = super.saveClickX;
			crossY = super.saveClickY;
			crossType = 2;
			crossIndex = 0;
			out.putOpCode(23);
			out.method431(cmd3 + baseY);
			out.method431(cmd1);
			out.method431(cmd2 + baseX);
		}
		if(action == 867)
		{
			if((cmd1 & 3) == 0)
				anInt1175++;
			if(anInt1175 >= 59)
			{
				out.putOpCode(200);
				out.putShort(25501);
				anInt1175 = 0;
			}
			out.putOpCode(43);
			out.method431(cmd3);
			out.method432(cmd1);
			out.method432(cmd2);
			atInventoryLoopCycle = 0;
			atInventoryInterface = cmd3;
			atInventoryIndex = cmd2;
			atInventoryInterfaceType = 2;
			if(RSInterface.cache[cmd3].parentId == openInterfaceID)
				atInventoryInterfaceType = 1;
			if(RSInterface.cache[cmd3].parentId == backDialogID)
				atInventoryInterfaceType = 3;
		}
		if(action == 543)
		{
			out.putOpCode(237);
			out.putShort(cmd2);
			out.method432(cmd1);
			out.putShort(cmd3);
			out.method432(anInt1137);
			atInventoryLoopCycle = 0;
			atInventoryInterface = cmd3;
			atInventoryIndex = cmd2;
			atInventoryInterfaceType = 2;
			if(RSInterface.cache[cmd3].parentId == openInterfaceID)
				atInventoryInterfaceType = 1;
			if(RSInterface.cache[cmd3].parentId == backDialogID)
				atInventoryInterfaceType = 3;
		}
		if(action == 606)
		{
			String s2 = menuActionName[actionIndex];
			int j2 = s2.indexOf("@whi@");
			if(j2 != -1)
				if(openInterfaceID == -1)
				{
					clearTopInterfaces();
					reportAbuseInput = s2.substring(j2 + 5).trim();
					canMute = false;
					for(int i3 = 0; i3 < RSInterface.cache.length; i3++)
					{
						if(RSInterface.cache[i3] == null || RSInterface.cache[i3].contentType != 600)
							continue;
						reportAbuseInterfaceID = openInterfaceID = RSInterface.cache[i3].parentId;
						break;
					}

				} else
				{
					pushMessage("", "Please close the interface you have open before using 'report abuse'", 0);
				}
		}
		if(action == 491)
		{
			Player class30_sub2_sub4_sub1_sub2_6 = playerArray[cmd1];
			if(class30_sub2_sub4_sub1_sub2_6 != null)
			{
				doWalkTo(2, 0, 1, 0, myPlayer.pathY[0], 1, 0, class30_sub2_sub4_sub1_sub2_6.pathY[0], myPlayer.pathX[0], false, class30_sub2_sub4_sub1_sub2_6.pathX[0]);
				crossX = super.saveClickX;
				crossY = super.saveClickY;
				crossType = 2;
				crossIndex = 0;
				out.putOpCode(14);
				out.method432(anInt1284);
				out.putShort(cmd1);
				out.putShort(anInt1285);
				out.method431(anInt1283);
			}
		}
		if(action == 639)
		{
			String s3 = menuActionName[actionIndex];
			int k2 = s3.indexOf("@whi@");
			if(k2 != -1)
			{
				long l4 = TextUtils.longForName(s3.substring(k2 + 5).trim());
				int k3 = -1;
				for(int i4 = 0; i4 < friendsCount; i4++)
				{
					if(friendsListAsLongs[i4] != l4)
						continue;
					k3 = i4;
					break;
				}

				if(k3 != -1 && friendsNodeIDs[k3] > 0)
				{
					inputTaken = true;
					dialogState = 0;
					promptRaised = true;
					promptInput = "";
					friendsListAction = 3;
					aLong953 = friendsListAsLongs[k3];
					promptMessage = "Enter message to send to " + friendsList[k3];
				}
			}
		}
		if(action == 454)
		{
			out.putOpCode(41);
			out.putShort(cmd1);
			out.method432(cmd2);
			out.method432(cmd3);
			atInventoryLoopCycle = 0;
			atInventoryInterface = cmd3;
			atInventoryIndex = cmd2;
			atInventoryInterfaceType = 2;
			if(RSInterface.cache[cmd3].parentId == openInterfaceID)
				atInventoryInterfaceType = 1;
			if(RSInterface.cache[cmd3].parentId == backDialogID)
				atInventoryInterfaceType = 3;
		}
		if(action == 478)
		{
			NPC class30_sub2_sub4_sub1_sub1_7 = npcArray[cmd1];
			if(class30_sub2_sub4_sub1_sub1_7 != null)
			{
				doWalkTo(2, 0, 1, 0, myPlayer.pathY[0], 1, 0, class30_sub2_sub4_sub1_sub1_7.pathY[0], myPlayer.pathX[0], false, class30_sub2_sub4_sub1_sub1_7.pathX[0]);
				crossX = super.saveClickX;
				crossY = super.saveClickY;
				crossType = 2;
				crossIndex = 0;
				if((cmd1 & 3) == 0)
					anInt1155++;
				if(anInt1155 >= 53)
				{
					out.putOpCode(85);
					out.writeWordBigEndian(66);
					anInt1155 = 0;
				}
				out.putOpCode(18);
				out.method431(cmd1);
			}
		}
		if(action == 113)
		{
			method66(cmd1, cmd3, cmd2);
			out.putOpCode(70);
			out.method431(cmd2 + baseX);
			out.putShort(cmd3 + baseY);
			out.method433(cmd1 >> 14 & 0x7fff);
		}
		if(action == 872)
		{
			method66(cmd1, cmd3, cmd2);
			out.putOpCode(234);
			out.method433(cmd2 + baseX);
			out.method432(cmd1 >> 14 & 0x7fff);
			out.method433(cmd3 + baseY);
		}
		if(action == 502)
		{
			method66(cmd1, cmd3, cmd2);
			out.putOpCode(132);
			out.method433(cmd2 + baseX);
			out.putShort(cmd1 >> 14 & 0x7fff);
			out.method432(cmd3 + baseY);
		}
		if(action == 1125)
		{
			ItemDef itemDef = ItemDef.getDef(cmd1);
			RSInterface class9_4 = RSInterface.cache[cmd3];
			String s5;
			if(class9_4 != null && class9_4.inventoryAmount[cmd2] >= 0x186a0)
				s5 = class9_4.inventoryAmount[cmd2] + " x " + itemDef.name;
			else
				if(itemDef.description != null)
					s5 = new String(itemDef.description);
				else
					s5 = "It's a " + itemDef.name + ".";
			pushMessage("", s5, 0);
		}
		if(action == 169)
		{
			out.putOpCode(185);
			out.putShort(cmd3);
			RSInterface class9_3 = RSInterface.cache[cmd3];
			if(class9_3.valueIndexArray != null && class9_3.valueIndexArray[0][0] == 5)
			{
				int l2 = class9_3.valueIndexArray[0][1];
				variousSettings[l2] = 1 - variousSettings[l2];
				handleActions(l2);
				redrawTabArea = true;
			}
		}
		if(action == 447)
		{
			itemSelected = 1;
			anInt1283 = cmd2;
			anInt1284 = cmd3;
			anInt1285 = cmd1;
			selectedItemName = ItemDef.getDef(cmd1).name;
			spellSelected = 0;
			redrawTabArea = true;
			return;
		}
		if(action == 1226)
		{
			int j1 = cmd1 >> 14 & 0x7fff;
		ObjectDef class46 = ObjectDef.getDef(j1);
		String s10;
		if(class46.description != null)
			s10 = new String(class46.description);
		else
			s10 = "It's a " + class46.name + ".";
		pushMessage("", s10, 0);
		}
		if(action == 244)
		{
			boolean flag7 = doWalkTo(2, 0, 0, 0, myPlayer.pathY[0], 0, 0, cmd3, myPlayer.pathX[0], false, cmd2);
			if(!flag7)
				flag7 = doWalkTo(2, 0, 1, 0, myPlayer.pathY[0], 1, 0, cmd3, myPlayer.pathX[0], false, cmd2);
			crossX = super.saveClickX;
			crossY = super.saveClickY;
			crossType = 2;
			crossIndex = 0;
			out.putOpCode(253);
			out.method431(cmd2 + baseX);
			out.method433(cmd3 + baseY);
			out.method432(cmd1);
		}
		if(action == 1448)
		{
			ItemDef itemDef_1 = ItemDef.getDef(cmd1);
			String s6;
			if(itemDef_1.description != null)
				s6 = new String(itemDef_1.description);
			else
				s6 = "It's a " + itemDef_1.name + ".";
			pushMessage("", s6, 0);
		}
		itemSelected = 0;
		spellSelected = 0;
		redrawTabArea = true;

	}

	private void method70()
	{
		anInt1251 = 0;
		int j = (myPlayer.currentX >> 7) + baseX;
		int k = (myPlayer.currentY >> 7) + baseY;
		if(j >= 3053 && j <= 3156 && k >= 3056 && k <= 3136)
			anInt1251 = 1;
		if(j >= 3072 && j <= 3118 && k >= 9492 && k <= 9535)
			anInt1251 = 1;
		if(anInt1251 == 1 && j >= 3139 && j <= 3199 && k >= 3008 && k <= 3062)
			anInt1251 = 0;
	}

	public void run()
	{
		if(drawFlames)
		{
			drawFlames();
		} else
		{
			super.run();
		}
	}

	private void build3dScreenMenu()
	{
		if(itemSelected == 0 && spellSelected == 0)
		{
			menuActionName[menuActionRow] = "Walk here";
			menuActionID[menuActionRow] = 516;
			menuActionCmd2[menuActionRow] = super.mouseX;
			menuActionCmd3[menuActionRow] = super.mouseY;
			menuActionRow++;
		}
		int j = -1;
		for(int k = 0; k < Model.anInt1687; k++) {
			int l = Model.anIntArray1688[k];
			int i1 = l & 0x7f;
			int j1 = l >> 7 & 0x7f;
			int k1 = l >> 29 & 3;
			int l1 = l >> 14 & 0x7fff;
			if(l == j)
				continue;
		j = l;
		if(k1 == 2 && worldController.getIdTagForPosition(floor_level, i1, j1, l) >= 0) {
			ObjectDef def = ObjectDef.getDef(l1);
			if(def.childrenIDs != null) {
				def = def.method580();
			}
			if(def == null)
				continue;
			if(itemSelected == 1)
			{
				menuActionName[menuActionRow] = "Use " + selectedItemName + " with @cya@" + def.name;
				menuActionID[menuActionRow] = 62;
				menuActionCmd1[menuActionRow] = l;
				menuActionCmd2[menuActionRow] = i1;
				menuActionCmd3[menuActionRow] = j1;
				menuActionRow++;
			} else
				if(spellSelected == 1)
				{
					if((spellUsableOn & 4) == 4)
					{
						menuActionName[menuActionRow] = spellTooltip + " @cya@" + def.name;
						menuActionID[menuActionRow] = 956;
						menuActionCmd1[menuActionRow] = l;
						menuActionCmd2[menuActionRow] = i1;
						menuActionCmd3[menuActionRow] = j1;
						menuActionRow++;
					}
				} else
				{
					if(def.actions != null)
					{
						for(int i2 = 4; i2 >= 0; i2--)
							if(def.actions[i2] != null)
							{
								menuActionName[menuActionRow] = def.actions[i2] + " @cya@" + def.name;
								if(i2 == 0)
									menuActionID[menuActionRow] = 502;
								if(i2 == 1)
									menuActionID[menuActionRow] = 900;
								if(i2 == 2)
									menuActionID[menuActionRow] = 113;
								if(i2 == 3)
									menuActionID[menuActionRow] = 872;
								if(i2 == 4)
									menuActionID[menuActionRow] = 1062;
								menuActionCmd1[menuActionRow] = l;
								menuActionCmd2[menuActionRow] = i1;
								menuActionCmd3[menuActionRow] = j1;
								menuActionRow++;
							}

					}
					menuActionName[menuActionRow] = "Examine @cya@" + def.name + " @gre@(@whi@" + l1 + "@gre@) (@whi@" + (i1 + baseX) + "," + (j1 + baseY) + "@gre@)";
					menuActionID[menuActionRow] = 1226;
					menuActionCmd1[menuActionRow] = def.type << 14;
					menuActionCmd2[menuActionRow] = i1;
					menuActionCmd3[menuActionRow] = j1;
					menuActionRow++;
				}
		}
		if(k1 == 1)
		{
			NPC npc = npcArray[l1];
			if(npc.desc.aByte68 == 1 && (npc.currentX & 0x7f) == 64 && (npc.currentY & 0x7f) == 64)
			{
				for(int j2 = 0; j2 < npcCount; j2++)
				{
					NPC npc2 = npcArray[npcIndices[j2]];
					if(npc2 != null && npc2 != npc && npc2.desc.aByte68 == 1 && npc2.currentX == npc.currentX && npc2.currentY == npc.currentY)
						buildAtNPCMenu(npc2.desc, npcIndices[j2], j1, i1);
				}

				for(int l2 = 0; l2 < playerCount; l2++)
				{
					Player player = playerArray[playerIndices[l2]];
					if(player != null && player.currentX == npc.currentX && player.currentY == npc.currentY)
						buildAtPlayerMenu(i1, playerIndices[l2], player, j1);
				}

			}
			buildAtNPCMenu(npc.desc, l1, j1, i1);
		}
		if(k1 == 0)
		{
			Player player = playerArray[l1];
			if((player.currentX & 0x7f) == 64 && (player.currentY & 0x7f) == 64)
			{
				for(int k2 = 0; k2 < npcCount; k2++)
				{
					NPC class30_sub2_sub4_sub1_sub1_2 = npcArray[npcIndices[k2]];
					if(class30_sub2_sub4_sub1_sub1_2 != null && class30_sub2_sub4_sub1_sub1_2.desc.aByte68 == 1 && class30_sub2_sub4_sub1_sub1_2.currentX == player.currentX && class30_sub2_sub4_sub1_sub1_2.currentY == player.currentY)
						buildAtNPCMenu(class30_sub2_sub4_sub1_sub1_2.desc, npcIndices[k2], j1, i1);
				}

				for(int i3 = 0; i3 < playerCount; i3++)
				{
					Player class30_sub2_sub4_sub1_sub2_2 = playerArray[playerIndices[i3]];
					if(class30_sub2_sub4_sub1_sub2_2 != null && class30_sub2_sub4_sub1_sub2_2 != player && class30_sub2_sub4_sub1_sub2_2.currentX == player.currentX && class30_sub2_sub4_sub1_sub2_2.currentY == player.currentY)
						buildAtPlayerMenu(i1, playerIndices[i3], class30_sub2_sub4_sub1_sub2_2, j1);
				}

			}
			buildAtPlayerMenu(i1, l1, player, j1);
		}
		if(k1 == 3)
		{
			Deque class19 = groundArray[floor_level][i1][j1];
			if(class19 != null)
			{
				for(Item item = (Item)class19.getFirst(); item != null; item = (Item)class19.getNext())
				{
					ItemDef itemDef = ItemDef.getDef(item.ID);
					if(itemSelected == 1)
					{
						menuActionName[menuActionRow] = "Use " + selectedItemName + " with @lre@" + itemDef.name;
						menuActionID[menuActionRow] = 511;
						menuActionCmd1[menuActionRow] = item.ID;
						menuActionCmd2[menuActionRow] = i1;
						menuActionCmd3[menuActionRow] = j1;
						menuActionRow++;
					} else
						if(spellSelected == 1)
						{
							if((spellUsableOn & 1) == 1)
							{
								menuActionName[menuActionRow] = spellTooltip + " @lre@" + itemDef.name;
								menuActionID[menuActionRow] = 94;
								menuActionCmd1[menuActionRow] = item.ID;
								menuActionCmd2[menuActionRow] = i1;
								menuActionCmd3[menuActionRow] = j1;
								menuActionRow++;
							}
						} else
						{
							for(int j3 = 4; j3 >= 0; j3--)
								if(itemDef.groundActions != null && itemDef.groundActions[j3] != null)
								{
									menuActionName[menuActionRow] = itemDef.groundActions[j3] + " @lre@" + itemDef.name;
									if(j3 == 0)
										menuActionID[menuActionRow] = 652;
									if(j3 == 1)
										menuActionID[menuActionRow] = 567;
									if(j3 == 2)
										menuActionID[menuActionRow] = 234;
									if(j3 == 3)
										menuActionID[menuActionRow] = 244;
									if(j3 == 4)
										menuActionID[menuActionRow] = 213;
									menuActionCmd1[menuActionRow] = item.ID;
									menuActionCmd2[menuActionRow] = i1;
									menuActionCmd3[menuActionRow] = j1;
									menuActionRow++;
								} else
									if(j3 == 2)
									{
										menuActionName[menuActionRow] = "Take @lre@" + itemDef.name;
										menuActionID[menuActionRow] = 234;
										menuActionCmd1[menuActionRow] = item.ID;
										menuActionCmd2[menuActionRow] = i1;
										menuActionCmd3[menuActionRow] = j1;
										menuActionRow++;
									}

							menuActionName[menuActionRow] = "Examine @lre@" + itemDef.name + " @gre@(@whi@" + item.ID + "@gre@)";
							menuActionID[menuActionRow] = 1448;
							menuActionCmd1[menuActionRow] = item.ID;
							menuActionCmd2[menuActionRow] = i1;
							menuActionCmd3[menuActionRow] = j1;
							menuActionRow++;
						}
				}

			}
		}
		}
	}

	public void cleanUpForQuit()
	{
		signlink.reporterror = false;
		try
		{
			if(socketStream != null)
				socketStream.close();
		}
		catch(Exception _ex) { }
		socketStream = null;
		stopMidi();
		if(mouseDetection != null)
			mouseDetection.running = false;
		mouseDetection = null;
		onDemandFetcher.disable();
		onDemandFetcher = null;
		aStream_834 = null;
		out = null;
		aStream_847 = null;
		in = null;
		anIntArray1234 = null;
		aByteArrayArray1183 = null;
		aByteArrayArray1247 = null;
		anIntArray1235 = null;
		anIntArray1236 = null;
		intGroundArray = null;
		byteGroundArray = null;
		worldController = null;
		aClass11Array1230 = null;
		anIntArrayArray901 = null;
		anIntArrayArray825 = null;
		bigX = null;
		bigY = null;
		texturePixels = null;
		tabArea = null;
		mapArea = null;
		gameArea = null;
		chatArea = null;
		aRSImageProducer_1123 = null;
		aRSImageProducer_1124 = null;
		aRSImageProducer_1125 = null;
		backLeftIP1 = null;
		backLeftIP2 = null;
		backRightIP1 = null;
		backRightIP2 = null;
		backTopIP1 = null;
		backVmidIP1 = null;
		backVmidIP2 = null;
		backVmidIP3 = null;
		backVmidIP2_2 = null;
		invBack = null;
		mapBack = null;
		chatBack = null;
		backBase1 = null;
		backBase2 = null;
		backHmid1 = null;
		sideIcons = null;
		redStone1 = null;
		redStone2 = null;
		redStone3 = null;
		redStone1_2 = null;
		redStone2_2 = null;
		redStone1_3 = null;
		redStone2_3 = null;
		redStone3_2 = null;
		redStone1_4 = null;
		redStone2_4 = null;
		compass = null;
		hitMarks = null;
		headIcons = null;
		crosses = null;
		mapDotItem = null;
		mapDotNPC = null;
		mapDotPlayer = null;
		mapDotFriend = null;
		mapDotTeam = null;
		mapScenes = null;
		mapFunctions = null;
		anIntArrayArray929 = null;
		playerArray = null;
		playerIndices = null;
		anIntArray894 = null;
		aStreamArray895s = null;
		anIntArray840 = null;
		npcArray = null;
		npcIndices = null;
		groundArray = null;
		deque = null;
		aClass19_1013 = null;
		aClass19_1056 = null;
		menuActionCmd2 = null;
		menuActionCmd3 = null;
		menuActionID = null;
		menuActionCmd1 = null;
		menuActionName = null;
		variousSettings = null;
		anIntArray1072 = null;
		anIntArray1073 = null;
		aClass30_Sub2_Sub1_Sub1Array1140 = null;
		aClass30_Sub2_Sub1_Sub1_1263 = null;
		friendsList = null;
		friendsListAsLongs = null;
		friendsNodeIDs = null;
		leftFlames = null;
		rightFlames = null;
		aRSImageProducer_1107 = null;
		aRSImageProducer_1108 = null;
		title = null;
		aRSImageProducer_1112 = null;
		aRSImageProducer_1113 = null;
		aRSImageProducer_1114 = null;
		aRSImageProducer_1115 = null;
		nullLoader();
		ObjectDef.nullLoader();
		NPCDef.nullLoader();
		ItemDef.nullLoader();
		Floor.cache = null;
		IdentityKit.cache = null;
		RSInterface.cache = null;
		Sequence.anims = null;
		SpotAnim.cache = null;
		SpotAnim.aMRUNodes_415 = null;
		Varp.cache = null;
		super.fullGameScreen = null;
		Player.mruNodes = null;
		Rasterizer.clearCache();
		SceneGraph.clearCache();
		Model.nullLoader();
		FrameHeader.nullLoader();
		System.gc();
	}

	private void printDebug()
	{
		System.out.println("============");
		System.out.println("flame-cycle:" + anInt1208);
		if(onDemandFetcher != null)
			System.out.println("Od-cycle:" + onDemandFetcher.onDemandCycle);
		System.out.println("loop-cycle:" + currentTime);
		System.out.println("draw-cycle:" + anInt1061);
		System.out.println("ptype:" + opCode);
		System.out.println("psize:" + size);
		if(socketStream != null)
			socketStream.printDebug();
		super.shouldDebug = true;
	}

	Component getGameComponent()
	{
		if(signlink.mainapp != null)
			return signlink.mainapp;
		if(super.mainFrame != null)
			return super.mainFrame;
		else
			return this;
	}

	private void method73()
	{
		do
		{
			int j = readCharacter();
			if(j == -1)
				break;
			if(openInterfaceID != -1 && openInterfaceID == reportAbuseInterfaceID)
			{
				if(j == 8 && reportAbuseInput.length() > 0)
					reportAbuseInput = reportAbuseInput.substring(0, reportAbuseInput.length() - 1);
				if((j >= 97 && j <= 122 || j >= 65 && j <= 90 || j >= 48 && j <= 57 || j == 32) && reportAbuseInput.length() < 12)
					reportAbuseInput += (char)j;
			} else
				if(promptRaised)
				{
					if(j >= 32 && j <= 122 && promptInput.length() < 80)
					{
						promptInput += (char)j;
						inputTaken = true;
					}
					if(j == 8 && promptInput.length() > 0)
					{
						promptInput = promptInput.substring(0, promptInput.length() - 1);
						inputTaken = true;
					}
					if(j == 13 || j == 10)
					{
						promptRaised = false;
						inputTaken = true;
						if(friendsListAction == 1)
						{
							long l = TextUtils.longForName(promptInput);
							addFriend(l);
						}
						if(friendsListAction == 2 && friendsCount > 0)
						{
							long l1 = TextUtils.longForName(promptInput);
							delFriend(l1);
						}
						if(friendsListAction == 3 && promptInput.length() > 0)
						{
							out.putOpCode(126);
							out.writeWordBigEndian(0);
							int k = out.offset;
							out.putLong(aLong953);
							TextInput.method526(promptInput, out);
							out.putBytes(out.offset - k);
							promptInput = TextInput.processText(promptInput);
							promptInput = Censor.censor(promptInput);
							pushMessage(TextUtils.fixName(TextUtils.nameForLong(aLong953)), promptInput, 6);
							if(privateChatMode == 2)
							{
								privateChatMode = 1;
								aBoolean1233 = true;
								out.putOpCode(95);
								out.writeWordBigEndian(publicChatMode);
								out.writeWordBigEndian(privateChatMode);
								out.writeWordBigEndian(tradeMode);
							}
						}
						if(friendsListAction == 4 && ignoreCount < 100)
						{
							long l2 = TextUtils.longForName(promptInput);
							addIgnore(l2);
						}
						if(friendsListAction == 5 && ignoreCount > 0)
						{
							long l3 = TextUtils.longForName(promptInput);
							delIgnore(l3);
						}
					}
				} else
					if(dialogState == 1)
					{
						if(j >= 48 && j <= 57 && amountOrNameInput.length() < 10)
						{
							amountOrNameInput += (char)j;
							inputTaken = true;
						}
						if(j == 8 && amountOrNameInput.length() > 0)
						{
							amountOrNameInput = amountOrNameInput.substring(0, amountOrNameInput.length() - 1);
							inputTaken = true;
						}
						if(j == 13 || j == 10)
						{
							if(amountOrNameInput.length() > 0)
							{
								int i1 = 0;
								try
								{
									i1 = Integer.parseInt(amountOrNameInput);
								}
								catch(Exception _ex) { }
								out.putOpCode(208);
								out.putInt(i1);
							}
							dialogState = 0;
							inputTaken = true;
						}
					} else
						if(dialogState == 2)
						{
							if(j >= 32 && j <= 122 && amountOrNameInput.length() < 12)
							{
								amountOrNameInput += (char)j;
								inputTaken = true;
							}
							if(j == 8 && amountOrNameInput.length() > 0)
							{
								amountOrNameInput = amountOrNameInput.substring(0, amountOrNameInput.length() - 1);
								inputTaken = true;
							}
							if(j == 13 || j == 10)
							{
								if(amountOrNameInput.length() > 0)
								{
									out.putOpCode(60);
									out.putLong(TextUtils.longForName(amountOrNameInput));
								}
								dialogState = 0;
								inputTaken = true;
							}
						} else
							if(backDialogID == -1)
							{
								if(j >= 32 && j <= 122 && inputString.length() < 80)
								{
									inputString += (char)j;
									inputTaken = true;
								}
								if(j == 8 && inputString.length() > 0)
								{
									inputString = inputString.substring(0, inputString.length() - 1);
									inputTaken = true;
								}
								if((j == 13 || j == 10) && inputString.length() > 0) {
									//if(myPrivilege == 2) {
										if(inputString.equals("::clientdrop"))
											dropClient();
										if(inputString.equals("::lag"))
											printDebug();
										if(inputString.equals("::prefetchmusic")) {
											for(int j1 = 0; j1 < onDemandFetcher.getVersionCount(2); j1++)
												onDemandFetcher.method563((byte)1, 2, j1);

										}
										if(inputString.equals("::fpson"))
											fpsOn = true;
										if(inputString.equals("::fpsoff"))
											fpsOn = false;
										if(inputString.equals("::noclip")) {
											for(int k1 = 0; k1 < 4; k1++) {
												for(int i2 = 1; i2 < 103; i2++) {
													for(int k2 = 1; k2 < 103; k2++)
														aClass11Array1230[k1].clipData[i2][k2] = 0;
												}
											}
										}
									//}
									if (inputString.startsWith("::interface")) {
										String[] args = inputString.split(" ");
										openInterfaceID = Integer.parseInt(args[1]);
									}
									if (inputString.startsWith("::full")) {
										String[] args = inputString.split(" ");
										fullscreenInterfaceID = Integer.parseInt(args[1]);
									}
									if (inputString.equals("::fixed")) {
										toggleSize(0);
									}
									if (inputString.equals("::resize")) {
										toggleSize(1);
									}
									if (inputString.equals("::full")) {
										toggleSize(2);
									}
									if (inputString.startsWith("::addobj")) {
										String[] args = inputString.split(" ");
										int id = Integer.parseInt(args[1]);
										int x = baseX + (myPlayer.currentX - 6 >> 7);
										int y = baseY + (myPlayer.currentY - 6 >> 7);
										addObject(id, x, y, floor_level, 0, 10);
										sendMessage("Object: " + id + " added at: " + x + ", " + y + ".");
									}
									if (inputString.startsWith("::objid")) {
										String name = inputString.substring(8);
										String ids = "";
										for (int index = 0; index < ObjectDef.totalObjects; index++) {
											ObjectDef object = ObjectDef.getDef(index);
											if (object != null && object.name != null) {
												if (object.name.equalsIgnoreCase(name)) {
													ids += (ids.length() == 0 ? "" : ",") + index;
												}
											}
										}
										if (ids.length() > 0) {
											sendMessage(ids);
										} else {
											sendMessage("No results found.");
										}
									}
									if (inputString.startsWith("::itemid")) {
										String name = inputString.substring(9);
										String ids = "";
										for (int index = 0; index < ItemDef.totalItems; index++) {
											ItemDef item = ItemDef.getDef(index);
											if (item != null && item.name != null) {
												if (item.name.equalsIgnoreCase(name)) {
													ids += (ids.length() == 0 ? "" : ",") + index;
												}
											}
										}
										if (ids.length() > 0) {
											sendMessage(ids);
										} else {
											sendMessage("No results found.");
										}
									}
									if (inputString.startsWith("::frame")) {
										String[] args = inputString.split(" ");
										setFrameVersion(Integer.parseInt(args[1]));
									}
									if(inputString.startsWith("::")) {
										out.putOpCode(103);
										out.writeWordBigEndian(inputString.length() - 1);
										out.putString(inputString.substring(2));
									} else {
										String s = inputString.toLowerCase();	
										int j2 = 0;
										if(s.startsWith("yellow:"))
										{
											j2 = 0;
											inputString = inputString.substring(7);
										} else
											if(s.startsWith("red:"))
											{
												j2 = 1;
												inputString = inputString.substring(4);
											} else
												if(s.startsWith("green:"))
												{
													j2 = 2;
													inputString = inputString.substring(6);
												} else
													if(s.startsWith("cyan:"))
													{
														j2 = 3;
														inputString = inputString.substring(5);
													} else
														if(s.startsWith("purple:"))
														{
															j2 = 4;
															inputString = inputString.substring(7);
														} else
															if(s.startsWith("white:"))
															{
																j2 = 5;
																inputString = inputString.substring(6);
															} else
																if(s.startsWith("flash1:"))
																{
																	j2 = 6;
																	inputString = inputString.substring(7);
																} else
																	if(s.startsWith("flash2:"))
																	{
																		j2 = 7;
																		inputString = inputString.substring(7);
																	} else
																		if(s.startsWith("flash3:"))
																		{
																			j2 = 8;
																			inputString = inputString.substring(7);
																		} else
																			if(s.startsWith("glow1:"))
																			{
																				j2 = 9;
																				inputString = inputString.substring(6);
																			} else
																				if(s.startsWith("glow2:"))
																				{
																					j2 = 10;
																					inputString = inputString.substring(6);
																				} else
																					if(s.startsWith("glow3:"))
																					{
																						j2 = 11;
																						inputString = inputString.substring(6);
																					}
										s = inputString.toLowerCase();
										int i3 = 0;
										if(s.startsWith("wave:"))
										{
											i3 = 1;
											inputString = inputString.substring(5);
										} else
											if(s.startsWith("wave2:"))
											{
												i3 = 2;
												inputString = inputString.substring(6);
											} else
												if(s.startsWith("shake:"))
												{
													i3 = 3;
													inputString = inputString.substring(6);
												} else
													if(s.startsWith("scroll:"))
													{
														i3 = 4;
														inputString = inputString.substring(7);
													} else
														if(s.startsWith("slide:"))
														{
															i3 = 5;
															inputString = inputString.substring(6);
														}
										out.putOpCode(4);
										out.writeWordBigEndian(0);
										int j3 = out.offset;
										out.method425(i3);
										out.method425(j2);
										aStream_834.offset = 0;
										TextInput.method526(inputString, aStream_834);
										out.method441(0, aStream_834.buffer, aStream_834.offset);
										out.putBytes(out.offset - j3);
										inputString = TextInput.processText(inputString);
										inputString = Censor.censor(inputString);
										myPlayer.textSpoken = inputString;
										myPlayer.anInt1513 = j2;
										myPlayer.anInt1531 = i3;
										myPlayer.textCycle = 150;
										pushMessage(getPrefix(myPrivilege) + myPlayer.name, myPlayer.textSpoken, 2);
										if(publicChatMode == 2) {
											publicChatMode = 3;
											aBoolean1233 = true;
											out.putOpCode(95);
											out.writeWordBigEndian(publicChatMode);
											out.writeWordBigEndian(privateChatMode);
											out.writeWordBigEndian(tradeMode);
										}
									}
									inputString = "";
									inputTaken = true;
								}
							}
		} while(true);
	}

	private void buildChatAreaMenu(int menuIndex) {
		int offset = 0;
		for(int index = 0; index < 100; index++) {
			if(chatMessages[index] == null) {
				continue;
			}
			int type = chatTypes[index];
			int y = (70 - offset * 14) + anInt1089 + 4;
			if(y < -20)
				break;
			String name = chatNames[index];
			if (name != null && name.indexOf("@") == 0) {
				name = name.substring(5);
			}
			if(type == 0) {
				offset++;
			}
			if((type == 1 || type == 2) && (type == 1 || publicChatMode == 0 || publicChatMode == 1 && isFriendOrSelf(name))) {
				if(menuIndex > y - 14 && menuIndex <= y && !name.equals(myPlayer.name)) {
					if(myPrivilege >= 1) {
						menuActionName[menuActionRow] = "Report abuse @whi@" + name;
						menuActionID[menuActionRow] = 606;
						menuActionRow++;
					}
					menuActionName[menuActionRow] = "Add ignore @whi@" + name;
					menuActionID[menuActionRow] = 42;
					menuActionRow++;
					menuActionName[menuActionRow] = "Add friend @whi@" + name;
					menuActionID[menuActionRow] = 337;
					menuActionRow++;
				}
				offset++;
			}
			if((type == 3 || type == 7) && splitPrivateChat == 0 && (type == 7 || privateChatMode == 0 || privateChatMode == 1 && isFriendOrSelf(name))) {
				if(menuIndex > y - 14 && menuIndex <= y) {
					if(myPrivilege >= 1) {
						menuActionName[menuActionRow] = "Report abuse @whi@" + name;
						menuActionID[menuActionRow] = 606;
						menuActionRow++;
					}
					menuActionName[menuActionRow] = "Add ignore @whi@" + name;
					menuActionID[menuActionRow] = 42;
					menuActionRow++;
					menuActionName[menuActionRow] = "Add friend @whi@" + name;
					menuActionID[menuActionRow] = 337;
					menuActionRow++;
				}
				offset++;
			}
			if(type == 4 && (tradeMode == 0 || tradeMode == 1 && isFriendOrSelf(name))) {
				if(menuIndex > y - 14 && menuIndex <= y) {
					menuActionName[menuActionRow] = "Accept trade @whi@" + name;
					menuActionID[menuActionRow] = 484;
					menuActionRow++;
				}
				offset++;
			}
			if((type == 5 || type == 6) && splitPrivateChat == 0 && privateChatMode < 2) {
				offset++;
			}
			if(type == 8 && (tradeMode == 0 || tradeMode == 1 && isFriendOrSelf(name))) {
				if(menuIndex > y - 14 && menuIndex <= y) {
					menuActionName[menuActionRow] = "Accept challenge @whi@" + name;
					menuActionID[menuActionRow] = 6;
					menuActionRow++;
				}
				offset++;
			}
		}

	}

	private void drawFriendsListOrWelcomeScreen(RSInterface rsi)
	{
		int j = rsi.contentType;
		if(j >= 1 && j <= 100 || j >= 701 && j <= 800)
		{
			if(j == 1 && anInt900 == 0)
			{
				rsi.disabledText = "Loading friend list";
				rsi.actionType = 0;
				return;
			}
			if(j == 1 && anInt900 == 1)
			{
				rsi.disabledText = "Connecting to friendserver";
				rsi.actionType = 0;
				return;
			}
			if(j == 2 && anInt900 != 2)
			{
				rsi.disabledText = "Please wait...";
				rsi.actionType = 0;
				return;
			}
			int k = friendsCount;
			if(anInt900 != 2)
				k = 0;
			if(j > 700)
				j -= 601;
			else
				j--;
			if(j >= k)
			{
				rsi.disabledText = "";
				rsi.actionType = 0;
				return;
			} else
			{
				rsi.disabledText = friendsList[j];
				rsi.actionType = 1;
				return;
			}
		}
		if(j >= 101 && j <= 200 || j >= 801 && j <= 900)
		{
			int l = friendsCount;
			if(anInt900 != 2)
				l = 0;
			if(j > 800)
				j -= 701;
			else
				j -= 101;
			if(j >= l)
			{
				rsi.disabledText = "";
				rsi.actionType = 0;
				return;
			}
			if(friendsNodeIDs[j] == 0)
				rsi.disabledText = "@red@Offline";
			else
				if(friendsNodeIDs[j] == nodeID)
					rsi.disabledText = "@gre@World-" + (friendsNodeIDs[j] - 9);
				else
					rsi.disabledText = "@yel@World-" + (friendsNodeIDs[j] - 9);
			rsi.actionType = 1;
			return;
		}
		if(j == 203)
		{
			int i1 = friendsCount;
			if(anInt900 != 2)
				i1 = 0;
			rsi.scrollMax = i1 * 15 + 20;
			if(rsi.scrollMax <= rsi.height)
				rsi.scrollMax = rsi.height + 1;
			return;
		}
		if(j >= 401 && j <= 500)
		{
			if((j -= 401) == 0 && anInt900 == 0)
			{
				rsi.disabledText = "Loading ignore list";
				rsi.actionType = 0;
				return;
			}
			if(j == 1 && anInt900 == 0)
			{
				rsi.disabledText = "Please wait...";
				rsi.actionType = 0;
				return;
			}
			int j1 = ignoreCount;
			if(anInt900 == 0)
				j1 = 0;
			if(j >= j1)
			{
				rsi.disabledText = "";
				rsi.actionType = 0;
				return;
			} else
			{
				rsi.disabledText = TextUtils.fixName(TextUtils.nameForLong(ignoreListAsLongs[j]));
				rsi.actionType = 1;
				return;
			}
		}
		if(j == 503)
		{
			rsi.scrollMax = ignoreCount * 15 + 20;
			if(rsi.scrollMax <= rsi.height)
				rsi.scrollMax = rsi.height + 1;
			return;
		}
		if(j == 327)
		{
			rsi.rotationX = 150;
			rsi.rotationY = (int)(Math.sin((double)currentTime / 40D) * 256D) & 0x7ff;
			if(aBoolean1031)
			{
				for(int k1 = 0; k1 < 7; k1++)
				{
					int l1 = anIntArray1065[k1];
					if(l1 >= 0 && !IdentityKit.cache[l1].method537())
						return;
				}

				aBoolean1031 = false;
				Model aclass30_sub2_sub4_sub6s[] = new Model[7];
				int i2 = 0;
				for(int j2 = 0; j2 < 7; j2++)
				{
					int k2 = anIntArray1065[j2];
					if(k2 >= 0)
						aclass30_sub2_sub4_sub6s[i2++] = IdentityKit.cache[k2].method538();
				}

				Model model = new Model(i2, aclass30_sub2_sub4_sub6s);
				for(int l2 = 0; l2 < 5; l2++)
					if(anIntArray990[l2] != 0)
					{
						model.changeModelColors(anIntArrayArray1003[l2][0], anIntArrayArray1003[l2][anIntArray990[l2]]);
						if(l2 == 1)
							model.changeModelColors(anIntArray1204[0], anIntArray1204[anIntArray990[l2]]);
					}

				model.method469();
				model.method470(Sequence.getSeq(myPlayer.standAnimIndex).frames[0]);
				model.method479(64, 850, -30, -50, -30, true);
				rsi.disabledMediaType = 5;
				rsi.disabledMediaId = 0;
				RSInterface.getModel(aBoolean994, model);
			}
			return;
		}
		if(j == 324)
		{
			if(aClass30_Sub2_Sub1_Sub1_931 == null)
			{
				aClass30_Sub2_Sub1_Sub1_931 = rsi.disabledSprite;
				aClass30_Sub2_Sub1_Sub1_932 = rsi.enabledSprite;
			}
			if(aBoolean1047)
			{
				rsi.disabledSprite = aClass30_Sub2_Sub1_Sub1_932;
				return;
			} else
			{
				rsi.disabledSprite = aClass30_Sub2_Sub1_Sub1_931;
				return;
			}
		}
		if(j == 325)
		{
			if(aClass30_Sub2_Sub1_Sub1_931 == null)
			{
				aClass30_Sub2_Sub1_Sub1_931 = rsi.disabledSprite;
				aClass30_Sub2_Sub1_Sub1_932 = rsi.enabledSprite;
			}
			if(aBoolean1047)
			{
				rsi.disabledSprite = aClass30_Sub2_Sub1_Sub1_931;
				return;
			} else
			{
				rsi.disabledSprite = aClass30_Sub2_Sub1_Sub1_932;
				return;
			}
		}
		if(j == 600)
		{
			rsi.disabledText = reportAbuseInput;
			if(currentTime % 20 < 10)
			{
				rsi.disabledText += "|";
				return;
			} else
			{
				rsi.disabledText += " ";
				return;
			}
		}
		if(j == 613)
			if(myPrivilege >= 1)
			{
				if(canMute)
				{
					rsi.disabledColor = 0xff0000;
					rsi.disabledText = "Moderator option: Mute player for 48 hours: <ON>";
				} else
				{
					rsi.disabledColor = 0xffffff;
					rsi.disabledText = "Moderator option: Mute player for 48 hours: <OFF>";
				}
			} else
			{
				rsi.disabledText = "";
			}
		if(j == 650 || j == 655)
			if(anInt1193 != 0)
			{
				String s;
				if(daysSinceLastLogin == 0)
					s = "earlier today";
				else
					if(daysSinceLastLogin == 1)
						s = "yesterday";
					else
						s = daysSinceLastLogin + " days ago";
				rsi.disabledText = "You last logged in " + s + " from: " + signlink.dns;
			} else
			{
				rsi.disabledText = "";
			}
		if(j == 651)
		{
			if(unreadMessages == 0)
			{
				rsi.disabledText = "0 unread messages";
				rsi.disabledColor = 0xffff00;
			}
			if(unreadMessages == 1)
			{
				rsi.disabledText = "1 unread message";
				rsi.disabledColor = 65280;
			}
			if(unreadMessages > 1)
			{
				rsi.disabledText = unreadMessages + " unread messages";
				rsi.disabledColor = 65280;
			}
		}
		if(j == 652)
			if(daysSinceRecovChange == 201)
			{
				if(membersInt == 1)
					rsi.disabledText = "@yel@This is a non-members world: @whi@Since you are a member we";
				else
					rsi.disabledText = "";
			} else
				if(daysSinceRecovChange == 200)
				{
					rsi.disabledText = "You have not yet set any password recovery questions.";
				} else
				{
					String s1;
					if(daysSinceRecovChange == 0)
						s1 = "Earlier today";
					else
						if(daysSinceRecovChange == 1)
							s1 = "Yesterday";
						else
							s1 = daysSinceRecovChange + " days ago";
					rsi.disabledText = s1 + " you changed your recovery questions";
				}
		if(j == 653)
			if(daysSinceRecovChange == 201)
			{
				if(membersInt == 1)
					rsi.disabledText = "@whi@recommend you use a members world instead. You may use";
				else
					rsi.disabledText = "";
			} else
				if(daysSinceRecovChange == 200)
					rsi.disabledText = "We strongly recommend you do so now to secure your account.";
				else
					rsi.disabledText = "If you do not remember making this change then cancel it immediately";
		if(j == 654)
		{
			if(daysSinceRecovChange == 201)
				if(membersInt == 1)
				{
					rsi.disabledText = "@whi@this world but member benefits are unavailable whilst here.";
					return;
				} else
				{
					rsi.disabledText = "";
					return;
				}
			if(daysSinceRecovChange == 200)
			{
				rsi.disabledText = "Do this from the 'account management' area on our front webpage";
				return;
			}
			rsi.disabledText = "Do this from the 'account management' area on our front webpage";
		}
	}

	private void drawSplitPrivateChat() {
		int offsetY = isFixed() ? 329 : getClientHeight() - 165;
		if(splitPrivateChat == 0) {
			return;
		}
		RSFont font = regular;
		int i = 0;
		if(anInt1104 != 0) {
			i = 1;
		}
		for(int index = 0; index < 100; index++)
			if(chatMessages[index] != null) {
				int type = chatTypes[index];
				String name = chatNames[index];
				String prefix = name;
				int rights = 0;
				if (name != null && name.indexOf("@") == 0) {
					name = name.substring(5);
					rights = getPrefixRights(prefix.substring(0, prefix.indexOf(name)));
				}
				if((type == 3 || type == 7) && (type == 7 || privateChatMode == 0 || privateChatMode == 1 && isFriendOrSelf(name))) {
					int y = offsetY - i * 13;
					int x = 4;
					font.drawBasicString("From", x, y, 0);
					font.drawBasicString("From", x, y - 1, 65535);
					x += font.getEffectTextWidth("From ");
					if(rights != 0) {
						modIcons[rights - 1].drawImage(x, y - 12);
						x += 14;
					}
					font.drawBasicString(name + ": " + chatMessages[index], x, y, 0);
					font.drawBasicString(name + ": " + chatMessages[index], x, y - 1, 65535);
					if(++i >= 5) {
						return;
					}
				}
				if(type == 5 && privateChatMode < 2) {
					int y = offsetY - i * 13;
					font.drawBasicString(chatMessages[index], 4, y, 0);
					font.drawBasicString(chatMessages[index], 4, y - 1, 65535);
					if(++i >= 5) {
						return;
					}
				}
				if(type == 6 && privateChatMode < 2) {
					int y = offsetY - i * 13;
					font.drawBasicString("To " + name + ": " + chatMessages[index], 4, y, 0);
					font.drawBasicString("To " + name + ": " + chatMessages[index], 4, y - 1, 65535);
					if(++i >= 5) {
						return;
					}
				}
			}

	}

	public void sendMessage(String message) {
		pushMessage("", message, 0);
	}

	private void pushMessage(String name, String message, int type) {
		if(type == 0 && dialogID != -1) {
			aString844 = message;
			super.clickMode3 = 0;
		}
		if(backDialogID == -1) {
			inputTaken = true;
		}
		for(int index = 99; index > 0; index--) {
			chatTypes[index] = chatTypes[index - 1];
			chatNames[index] = chatNames[index - 1];
			chatMessages[index] = chatMessages[index - 1];
		}
		chatTypes[0] = type;
		chatNames[0] = name;
		chatMessages[0] = message;
	}

	private void processTabClick() {
		if (!isFixed()) {
			int startX;
			int startY;
			int endY;
			int tab;
			if (super.clickMode3 == 1) {
				tab = 0;
				startX = getTabRowHeight() > 40 ? getClientWidth() - getTabWidth() * 7 : getClientWidth() - getTabWidth() * 14;
				startY = getClientHeight() - getTabRowHeight();
				endY = getTabRowHeight() > 40 ? getTabRowHeight() / 2 : getTabRowHeight();
				if (clickInRegion(startX, startX + getTabWidth(), startY, startY + endY) && tabInterfaceIDs[tab] != -1) {
					redrawTabArea = true;
					tabID = tab;
					tabAreaAltered = true;
				}
				tab += 1;
				startX += getTabWidth();
				if (clickInRegion(startX, startX + getTabWidth(), startY, startY + endY) && tabInterfaceIDs[tab] != -1) {
					redrawTabArea = true;
					tabID = tab;
					tabAreaAltered = true;
				}
				tab += 1;
				startX += getTabWidth();
				if (clickInRegion(startX, startX + getTabWidth(), startY, startY + endY) && tabInterfaceIDs[tab] != -1) {
					redrawTabArea = true;
					tabID = tab;
					tabAreaAltered = true;
				}
				tab += 1;
				startX += getTabWidth();
				if (clickInRegion(startX, startX + getTabWidth(), startY, startY + endY) && tabInterfaceIDs[tab] != -1) {
					redrawTabArea = true;
					tabID = tab;
					tabAreaAltered = true;
				}
				tab += 1;
				startX += getTabWidth();
				if (clickInRegion(startX, startX + getTabWidth(), startY, startY + endY) && tabInterfaceIDs[tab] != -1) {
					redrawTabArea = true;
					tabID = tab;
					tabAreaAltered = true;
				}
				tab += 1;
				startX += getTabWidth();
				if (clickInRegion(startX, startX + getTabWidth(), startY, startY + endY) && tabInterfaceIDs[tab] != -1) {
					redrawTabArea = true;
					tabID = tab;
					tabAreaAltered = true;
				}
				tab += 1;
				startX += getTabWidth();
				if (clickInRegion(startX, startX + getTabWidth(), startY, startY + endY) && tabInterfaceIDs[tab] != -1) {
					redrawTabArea = true;
					tabID = tab;
					tabAreaAltered = true;
				}
				tab += 1;
				if (getTabRowHeight() > 40) {
					startX = getClientWidth() - getTabWidth() * 7;
					startY = getClientHeight() - (getTabRowHeight() / 2);
				} else {
					startX += getTabWidth();
				}
				if (clickInRegion(startX, startX + getTabWidth(), startY, startY + endY) && tabInterfaceIDs[tab] != -1) {
					redrawTabArea = true;
					tabID = tab;
					tabAreaAltered = true;
				}
				tab += 1;
				startX += getTabWidth();
				if (clickInRegion(startX, startX + getTabWidth(), startY, startY + endY) && tabInterfaceIDs[tab] != -1) {
					redrawTabArea = true;
					tabID = tab;
					tabAreaAltered = true;
				}
				tab += 1;
				startX += getTabWidth();
				if (clickInRegion(startX, startX + getTabWidth(), startY, startY + endY) && tabInterfaceIDs[tab] != -1) {
					redrawTabArea = true;
					tabID = tab;
					tabAreaAltered = true;
				}
				tab += 1;
				startX += getTabWidth();
				//logout button
				//if (clickInRegion(startX, startX + getTabWidth(), startY, startY + endY) && tabInterfaceIDs[tab] != -1) {
				if (clickInRegion(getClientWidth() - 21, getClientWidth(), 0, 21) && tabInterfaceIDs[tab] != -1) {
					redrawTabArea = true;
					tabID = tab;
					tabAreaAltered = true;
				}
				tab += 1;
				startX += getTabWidth();
				if (clickInRegion(startX, startX + getTabWidth(), startY, startY + endY) && tabInterfaceIDs[tab] != -1) {
					redrawTabArea = true;
					tabID = tab;
					tabAreaAltered = true;
				}
				tab += 1;
				startX += getTabWidth();
				if (clickInRegion(startX, startX + getTabWidth(), startY, startY + endY) && tabInterfaceIDs[tab] != -1) {
					redrawTabArea = true;
					tabID = tab;
					tabAreaAltered = true;
				}
				tab += 1;
				startX += getTabWidth();
				if (clickInRegion(startX, startX + getTabWidth(), startY, startY + endY) && tabInterfaceIDs[tab] != -1) {
					redrawTabArea = true;
					tabID = tab;
					tabAreaAltered = true;
				}
			}
		}
		if (isFixed() && getFrameVersion() == 317) {
			if(super.clickMode3 == 1) {
				if (clickInRegion(539, 573, 169, 205) && tabInterfaceIDs[0] != -1) {
					redrawTabArea = true;
					tabID = 0;
					tabAreaAltered = true;
				}
				if (clickInRegion(569, 599, 168, 205) && tabInterfaceIDs[1] != -1) {
					redrawTabArea = true;
					tabID = 1;
					tabAreaAltered = true;
				}
				if (clickInRegion(597, 627, 168, 205) && tabInterfaceIDs[2] != -1) {
					redrawTabArea = true;
					tabID = 2;
					tabAreaAltered = true;
				}
				if (clickInRegion(625, 669, 168, 203) && tabInterfaceIDs[3] != -1) {
					redrawTabArea = true;
					tabID = 3;
					tabAreaAltered = true;
				}
				if (clickInRegion(666, 696, 168, 205) && tabInterfaceIDs[4] != -1) {
					redrawTabArea = true;
					tabID = 4;
					tabAreaAltered = true;
				}
				if (clickInRegion(694, 724, 168, 205) && tabInterfaceIDs[5] != -1) {
					redrawTabArea = true;
					tabID = 5;
					tabAreaAltered = true;
				}
				if (clickInRegion(722, 756, 169, 205) && tabInterfaceIDs[6] != -1) {
					redrawTabArea = true;
					tabID = 6;
					tabAreaAltered = true;
				}
				if (clickInRegion(540, 574, 466, 502) && tabInterfaceIDs[7] != -1) {
					redrawTabArea = true;
					tabID = 7;
					tabAreaAltered = true;
				}
				if (clickInRegion(572, 602, 466, 503) && tabInterfaceIDs[8] != -1) {
					redrawTabArea = true;
					tabID = 8;
					tabAreaAltered = true;
				}
				if (clickInRegion(599, 629, 466, 502) && tabInterfaceIDs[9] != -1) {
					redrawTabArea = true;
					tabID = 9;
					tabAreaAltered = true;
				}
				if (clickInRegion(627, 671, 467, 502) && tabInterfaceIDs[10] != -1) {
					redrawTabArea = true;
					tabID = 10;
					tabAreaAltered = true;
				}
				if (clickInRegion(669, 699, 466, 503) && tabInterfaceIDs[11] != -1) {
					redrawTabArea = true;
					tabID = 11;
					tabAreaAltered = true;
				}
				if (clickInRegion(696, 726, 466, 503) && tabInterfaceIDs[12] != -1) {
					redrawTabArea = true;
					tabID = 12;
					tabAreaAltered = true;
				}
				if (clickInRegion(724, 758, 466, 502) && tabInterfaceIDs[13] != -1) {
					redrawTabArea = true;
					tabID = 13;
					tabAreaAltered = true;
				}
			}
		}
	}

	private void resetImageProducers2() {
		if(chatArea != null)
			return;
		nullLoader();
		super.fullGameScreen = null;
		aRSImageProducer_1107 = null;
		aRSImageProducer_1108 = null;
		title = null;
		leftFlames = null;
		rightFlames = null;
		aRSImageProducer_1112 = null;
		aRSImageProducer_1113 = null;
		aRSImageProducer_1114 = null;
		aRSImageProducer_1115 = null;
		chatArea = new RSImageProducer(479, 96, getGameComponent());
		mapArea = new RSImageProducer(172, 156, getGameComponent());
		RSDrawingArea.setAllPixelsToZero();
		mapBack.drawImage(0, 0);
		tabArea = new RSImageProducer(190, 261, getGameComponent());
		gameArea = new RSImageProducer(gameAreaWidth, gameAreaHeight, getGameComponent());
		RSDrawingArea.setAllPixelsToZero();
		aRSImageProducer_1123 = new RSImageProducer(496, 50, getGameComponent());
		aRSImageProducer_1124 = new RSImageProducer(269, 37, getGameComponent());
		aRSImageProducer_1125 = new RSImageProducer(249, 45, getGameComponent());
		welcomeScreenRaised = true;
	}

	@SuppressWarnings("unused")
	private String getDocumentBaseHost()
	{
		if(signlink.mainapp != null)
			return signlink.mainapp.getDocumentBase().getHost().toLowerCase();
		if(super.mainFrame != null)
			return "";
		else
			return "";
	}

	private void method81(RSImage sprite, int j, int k)
	{
		int l = k * k + j * j;
		if(l > 4225 && l < 0x15f90)
		{
			int i1 = minimapInt1 + minimapInt2 & 0x7ff;
			int j1 = Model.SINE[i1];
			int k1 = Model.COSINE[i1];
			j1 = (j1 * 256) / (minimapInt3 + 256);
			k1 = (k1 * 256) / (minimapInt3 + 256);
			int l1 = j * j1 + k * k1 >> 16;
		int i2 = j * k1 - k * j1 >> 16;
			double d = Math.atan2(l1, i2);
			int j2 = (int)(Math.sin(d) * 63D);
			int k2 = (int)(Math.cos(d) * 57D);
			mapEdge.method353(83 - k2 - 20, d, (94 + j2 + 4) - 10);
		} else
		{
			markMinimap(sprite, k, j);
		}
	}

	public void processGameAreaClick() {
		int startX = isFixed() ? 4 : 0;
		int endX = isFixed() ? 516 : getClientWidth();
		int startY = isFixed() ? 4 : 0;
		int endY = isFixed() ? 338 : getClientHeight();
		if (mouseInRegion(0, 518, getClientHeight() - 165, getClientHeight()) && showChatArea) {
			return;
		} else if (mouseInRegion(0, 518, getClientHeight() - 22, getClientHeight())) {
			return;
		}
		if (mouseInRegion(getClientWidth() - 204, getClientWidth(), getClientHeight() - 274 - getTabRowHeight(), getClientHeight())) {
			return;
		}
		if (mouseInRegion(getClientWidth() - (getTabRowHeight() > 40 ? getTabWidth() * 7 : getTabWidth() * 14), getClientWidth(), getClientHeight() - getTabRowHeight(), getClientHeight())) {
			return;
		}
		if (mouseInRegion(getClientWidth() - 21, getClientWidth(), 0, 21)) {
			return;
		}
		if (mouseInRegion(startX, endX, startY, endY)) {
			if(openInterfaceID != -1) {
				RSInterface rsi = RSInterface.cache[openInterfaceID];
				int x = isFixed() ? 0 : (getClientWidth() / 2) - (rsi.width / 2);
				int y = isFixed() ? 0 : (getClientHeight() / 2) - (rsi.height / 2);
				drawInterface(RSInterface.cache[openInterfaceID], x, y, 0);
				buildInterfaceMenu(rsi, x, y, super.mouseX, super.mouseY, 0);
			} else {
				build3dScreenMenu();
			}
		}
		if(anInt886 != anInt1026) {
			anInt1026 = anInt886;
		}
        if (anInt1315 != anInt1129) {
            anInt1129 = anInt1315;
        }
		anInt886 = 0;
        anInt1315 = 0;
	}

	public final int VIEW_ALL = 901;
	public final int VIEW_GAME = 902;
	public final int VIEW_PUBLIC = 903;
	public final int ON_PUBLIC = 904;
	public final int FRIENDS_PUBLIC = 905;
	public final int OFF_PUBLIC = 906;
	public final int HIDE_PUBLIC = 907;
	public final int VIEW_PRIVATE = 908;
	public final int ON_PRIVATE = 909;
	public final int FRIENDS_PRIVATE = 910;
	public final int OFF_PRIVATE = 911;
	public final int VIEW_CLAN = 912;
	public final int ON_CLAN = 913;
	public final int FRIENDS_CLAN = 914;
	public final int OFF_CLAN = 915;
	public final int VIEW_YELL = 916;
	public final int ON_YELL = 917;
	public final int FRIENDS_YELL = 918;
	public final int OFF_YELL = 919;
	public final int VIEW_TRADE = 920;
	public final int ON_TRADE = 921;
	public final int FRIENDS_TRADE = 922;
	public final int OFF_TRADE = 923;
	public final int REPORT_ABUSE = 924;

	public int chatModeHover = -1;
	public void processRightClickChatMode() {
		if (!isFixed() || getFrameVersion() != 317) {
			int x = 5;
			int y = getClientHeight() - 22;
			chatModeHover = -1;
			if (mouseInRegion(x, x + 56, y, y + 22)) {
				chatModeHover = ALL;
				menuActionName[1] = "View All";
				menuActionID[1] = VIEW_ALL;
				menuActionRow = 2;
			}
			x += 57;
			if (mouseInRegion(x, x + 56, y, y + 22)) {
				chatModeHover = GAME;
				menuActionName[1] = "View Game";
				menuActionID[1] = VIEW_GAME;
				menuActionRow = 2;
			}
			x += 57;
			if (mouseInRegion(x, x + 56, y, y + 22)) {
				chatModeHover = PUBLIC;
				menuActionName[5] = "View Public";
				menuActionID[5] = VIEW_PUBLIC;
				menuActionName[4] = "On Public";
				menuActionID[4] = ON_PUBLIC;
				menuActionName[3] = "Friends Public";
				menuActionID[3] = FRIENDS_PUBLIC;
				menuActionName[2] = "Off Public";
				menuActionID[2] = OFF_PUBLIC;
				menuActionName[1] = "Hide Public";
				menuActionID[1] = HIDE_PUBLIC;
				menuActionRow = 6;
			}
			x += 57;
			if (mouseInRegion(x, x + 56, y, y + 22)) {
				chatModeHover = PRIVATE;
				menuActionName[4] = "View Private";
				menuActionID[4] = VIEW_PRIVATE;
				menuActionName[3] = "On Private";
				menuActionID[3] = ON_PRIVATE;
				menuActionName[2] = "Friends Private";
				menuActionID[2] = FRIENDS_PRIVATE;
				menuActionName[1] = "Off Public";
				menuActionID[1] = OFF_PRIVATE;
				menuActionRow = 5;
			}
			x += 57;
			if (mouseInRegion(x, x + 56, y, y + 22)) {
				chatModeHover = CLAN;
				menuActionName[4] = "View Clan";
				menuActionID[4] = VIEW_CLAN;
				menuActionName[3] = "On Clan";
				menuActionID[3] = ON_CLAN;
				menuActionName[2] = "Friends Clan";
				menuActionID[2] = FRIENDS_CLAN;
				menuActionName[1] = "Off Clan";
				menuActionID[1] = OFF_CLAN;
				menuActionRow = 5;
			}
			x += 57;
			if (mouseInRegion(x, x + 56, y, y + 22)) {
				chatModeHover = YELL;
				menuActionName[4] = "View Yell";
				menuActionID[4] = VIEW_YELL;
				menuActionName[3] = "On Yell";
				menuActionID[3] = ON_YELL;
				menuActionName[2] = "Friends Yell";
				menuActionID[2] = FRIENDS_YELL;
				menuActionName[1] = "Off Yell";
				menuActionID[1] = OFF_YELL;
				menuActionRow = 5;
			}
			x += 57;
			if (mouseInRegion(x, x + 56, y, y + 22)) {
				chatModeHover = TRADE;
				menuActionName[4] = "View Trade";
				menuActionID[4] = VIEW_TRADE;
				menuActionName[3] = "On Trade";
				menuActionID[3] = ON_TRADE;
				menuActionName[2] = "Friends Trade";
				menuActionID[2] = FRIENDS_TRADE;
				menuActionName[1] = "Off Trade";
				menuActionID[1] = OFF_TRADE;
				menuActionRow = 5;
			}
			x += 57;
			if (mouseInRegion(x, x + 110, y, y + 22)) {
				chatModeHover = REPORT;
				menuActionName[1] = "Report Abuse";
				menuActionID[1] = REPORT_ABUSE;
				menuActionRow = 2;
			}
		}
	}

	public void processChatAreaClick() {
		int startX = isFixed() && getFrameVersion() == 317 ? 17 : 8;
		int endX = isFixed() && getFrameVersion() == 317 ? 496 : 509;
		int startY = isFixed() && getFrameVersion() == 317 ? 357 : getClientHeight() - 165;
		int endY = isFixed() && getFrameVersion() == 317 ? 453 : getClientHeight() - 40;
		if(super.mouseX > startX && super.mouseY > startY && super.mouseX < endX && super.mouseY < endY) {
			if(backDialogID != -1) {
				buildInterfaceMenu(RSInterface.cache[backDialogID], startX, startY, super.mouseX, super.mouseY, 0);
			} else {
				buildChatAreaMenu(super.mouseY - startY - (!isFixed() || getFrameVersion() != 317 ? 43 : -3));
			}
		}
		if(backDialogID != -1 && anInt886 != anInt1039) {
			inputTaken = true;
			anInt1039 = anInt886;
		}
        if (backDialogID != -1 && anInt1315 != anInt1500) {
            inputTaken = true;
            anInt1500 = anInt1315;
        }
	}

	public void processTabAreaClick() {
		int startX = isFixed() && getFrameVersion() == 317 ? 553 : getClientWidth() - 204 + 7;
		int endX = isFixed() && getFrameVersion() == 317 ? 743 : getClientWidth() - 7;
		int startY = isFixed() && getFrameVersion() == 317 ? 205 : getClientHeight() - 274 - (getTabRowHeight() - 7);
		int endY = isFixed() && getFrameVersion() == 317 ? 466 : getClientHeight() - getTabRowHeight() - 7;
		if(super.mouseX > startX && super.mouseY > startY && super.mouseX < endX && super.mouseY < endY) {
			if(invOverlayInterfaceID != -1) {
				buildInterfaceMenu(RSInterface.cache[invOverlayInterfaceID], startX, startY, super.mouseX, super.mouseY, 0);
			} else if(tabInterfaceIDs[tabID] != -1) {
				buildInterfaceMenu(RSInterface.cache[tabInterfaceIDs[tabID]], startX, startY, super.mouseX, super.mouseY, 0);
			}
		}
		if(anInt886 != anInt1048) {
			redrawTabArea = true;
			anInt1048 = anInt886;
		}
        if (anInt1315 != anInt1044) {
            redrawTabArea = true;
            tabAreaAltered = true;
            anInt1044 = anInt1315;
        }
		anInt886 = 0;
        anInt1315 = 0;
	}

	private void processRightClick() {
		if(activeInterfaceType != 0) {
			return;
		}
		menuActionName[0] = "Cancel";
		menuActionID[0] = 1107;
		menuActionRow = 1;
		if (fullscreenInterfaceID != -1) {
            anInt886 = 0;
            anInt1315 = 0;
            int x = isFixed() ? 8 : (getClientWidth() / 2) - (765 / 2);
            int y = isFixed() ? 8 : 8 + (getClientHeight() / 2) - (503 / 2);
            buildInterfaceMenu(RSInterface.cache[fullscreenInterfaceID], x, y, super.mouseX, super.mouseY, 0);
            if (anInt886 != anInt1026) {
                anInt1026 = anInt886;
            }
            if (anInt1315 != anInt1129) {
                anInt1129 = anInt1315;
            }
            return;
        }
		buildSplitPrivateChatMenu();
		anInt886 = 0;
        anInt1315 = 0;
		processGameAreaClick();
		processTabAreaClick();
		processChatAreaClick();
        processRightClickChatMode();
		boolean flag = false;
		while(!flag) {
			flag = true;
			for(int index = 0; index < menuActionRow - 1; index++)
				if(menuActionID[index] < 1000 && menuActionID[index + 1] > 1000) {
					String name = menuActionName[index];
					menuActionName[index] = menuActionName[index + 1];
					menuActionName[index + 1] = name;
					int action = menuActionID[index];
					menuActionID[index] = menuActionID[index + 1];
					menuActionID[index + 1] = action;
					action = menuActionCmd2[index];
					menuActionCmd2[index] = menuActionCmd2[index + 1];
					menuActionCmd2[index + 1] = action;
					action = menuActionCmd3[index];
					menuActionCmd3[index] = menuActionCmd3[index + 1];
					menuActionCmd3[index + 1] = action;
					action = menuActionCmd1[index];
					menuActionCmd1[index] = menuActionCmd1[index + 1];
					menuActionCmd1[index + 1] = action;
					flag = false;
				}
		}
	}

	private int method83(int i, int j, int k) {
		int l = 256 - k;
		return ((i & 0xff00ff) * l + (j & 0xff00ff) * k & 0xff00ff00) + ((i & 0xff00) * l + (j & 0xff00) * k & 0xff0000) >> 8;
	}

	public int messageState = 0;
	public int USERNAME = 1;
	public int PASSWORD = 2;
	public int BOTH = 3;

	/**
	 * Returns your formatted username.
	 * @return
	 */
	public String getUsername() {
		return TextUtils.fixName(myUsername);
	}

	/**
	 * Returns the hash for the specified password.
	 * @param password
	 * @return
	 */
	public String getPasswordHash(String password) {
		return MD5.getHash(MD5.getHash(password));
	}

	/**
	 * Sets the username to the formatted string.
	 * @param username
	 */
	public void setUsername(String username) {
		myUsername = TextUtils.fixName(username);
	}

	private void login(String username, String password, boolean flag, boolean saved) {
		username = TextUtils.fixName(username);
		signlink.errorname = username;
		if (username.length() == 0 && password.length() == 0) {
			loginMessage1 = "Please enter valid login details.";
			loginMessage2 = "";
			return;
		}
		if (username.length() == 0 && password.length() > 0) {
			String message = "Please enter a valid username.";
			if (loginMessage1.length() == 0 || messageState == USERNAME) {
				loginMessage1 = message;
				loginMessage2 = "";
				messageState = USERNAME;
			} else {
				loginMessage2 = message;
			}
			return;
		}
		if (password.length() == 0 && username.length() > 0) {
			String message = "Please enter a valid password.";
			if (loginMessage1.length() == 0 || messageState == PASSWORD) {
				loginMessage1 = message;
				loginMessage2 = "";
				messageState = PASSWORD;
			} else {
				loginMessage2 = message;
			}
			return;
		}
		if (password.length() < 5 && !saved) {
			String message = "Your password is too short!";
			if (loginMessage1.length() == 0 || messageState == PASSWORD) {
				loginMessage1 = message;
				loginMessage2 = "";
				messageState = PASSWORD;
			} else {
				loginMessage2 = message;
			}
			return;
		}
		if (password.length() > 20 && !saved) {
			String message = "Your password is too long!";
			if (loginMessage1.length() == 0 || messageState == PASSWORD) {
				loginMessage1 = message;
				loginMessage2 = "";
				messageState = PASSWORD;
			} else {
				loginMessage2 = message;
			}
			return;
		}
		try {
			if(!flag) {
				loginMessage1 = "Connecting to server...";
				loginMessage2 = "";
				displayTitleScreen(true);
			}
			socketStream = new RSSocket(this, openSocket(Constants.PORT + portOff));
			long nameLong = TextUtils.longForName(username);
			int i = (int)(nameLong >> 16 & 31L);
			out.offset = 0;
			out.writeWordBigEndian(14);
			out.writeWordBigEndian(i);
			socketStream.queueBytes(2, out.buffer);
			for(int index = 0; index < 8; index++) {
				socketStream.read();
			}
			int response = socketStream.read();
			int origResponse = response;
			if(response == 0) {
				socketStream.flushInputStream(in.buffer, 8);
				in.offset = 0;
				aLong1215 = in.getLong();
				int data[] = new int[4];
				data[0] = (int)(Math.random() * 99999999D);
				data[1] = (int)(Math.random() * 99999999D);
				data[2] = (int)(aLong1215 >> 32);
				data[3] = (int)aLong1215;
				out.offset = 0;
				out.writeWordBigEndian(10);
				out.putInt(data[0]);
				out.putInt(data[1]);
				out.putInt(data[2]);
				out.putInt(data[3]);
				out.putInt(signlink.uid);
				out.putString(username);
				out.putString(saved ? password : getPasswordHash(password));
				out.doKeys();
				aStream_847.offset = 0;
				if(flag)
					aStream_847.writeWordBigEndian(18);
				else
					aStream_847.writeWordBigEndian(16);
				aStream_847.writeWordBigEndian(out.offset + 36 + 1 + 1 + 2);
				aStream_847.writeWordBigEndian(255);
				aStream_847.putShort(Constants.CLIENT_VERSION);
				aStream_847.writeWordBigEndian(lowMem ? 1 : 0);
				for(int index = 0; index < 9; index++) {
					aStream_847.putInt(expectedCRCs[index]);
				}
				aStream_847.putBytes(out.buffer, out.offset, 0);
				out.encryption = new ISAACRandomGen(data);
				for(int index = 0; index < 4; index++) {
					data[index] += 50;
				}
				encryption = new ISAACRandomGen(data);
				socketStream.queueBytes(aStream_847.offset, aStream_847.buffer);
				response = socketStream.read();
			}
			if(response == 1) {
				try {
					Thread.sleep(2000L);
				} catch(Exception _ex) {
				}
				login(username, password, flag, saved);
				return;
			}
			if(response == 2) {
				Accounts.add(username, getPasswordHash(password), 1);
				Accounts.write();
				myUsername = username;
				myPassword = saved ? "" : password;
				myPrivilege = socketStream.read();
				flagged = socketStream.read() == 1;
				aLong1220 = 0L;
				anInt1022 = 0;
				mouseDetection.coordsIndex = 0;
				super.awtFocus = true;
				aBoolean954 = true;
				loggedIn = true;
				out.offset = 0;
				in.offset = 0;
				opCode = -1;
				anInt841 = -1;
				anInt842 = -1;
				anInt843 = -1;
				size = 0;
				anInt1009 = 0;
				anInt1104 = 0;
				anInt1011 = 0;
				anInt855 = 0;
				menuActionRow = 0;
				menuOpen = false;
				super.idleTime = 0;
				for(int index = 0; index < 100; index++) {
					chatMessages[index] = null;
				}
				itemSelected = 0;
				spellSelected = 0;
				loadingStage = 0;
				anInt1062 = 0;
				anInt1278 = (int)(Math.random() * 100D) - 50;
				anInt1131 = (int)(Math.random() * 110D) - 55;
				anInt896 = (int)(Math.random() * 80D) - 40;
				minimapInt2 = (int)(Math.random() * 120D) - 60;
				minimapInt3 = (int)(Math.random() * 30D) - 20;
				minimapInt1 = (int)(Math.random() * 20D) - 10 & 0x7ff;
				anInt1021 = 0;
				anInt985 = -1;
				destX = 0;
				destY = 0;
				playerCount = 0;
				npcCount = 0;
				for(int index = 0; index < maxPlayers; index++) {
					playerArray[index] = null;
					aStreamArray895s[index] = null;
				}
				for(int index = 0; index < 16384; index++) {
					npcArray[index] = null;
				}
				myPlayer = playerArray[myPlayerIndex] = new Player();
				aClass19_1013.removeAll();
				aClass19_1056.removeAll();
				for(int z = 0; z < 4; z++) {
					for(int x = 0; x < 104; x++) {
						for(int y = 0; y < 104; y++) {
							groundArray[z][x][y] = null;
						}
					}
				}
				deque = new Deque();
				anInt900 = 0;
				friendsCount = 0;
				dialogID = -1;
				backDialogID = -1;
				openInterfaceID = -1;
				fullscreenInterfaceID = -1;
				invOverlayInterfaceID = -1;
				anInt1018 = -1;
				aBoolean1149 = false;
				tabID = 3;
				dialogState = 0;
				menuOpen = false;
				promptRaised = false;
				aString844 = null;
				anInt1055 = 0;
				anInt1054 = -1;
				aBoolean1047 = true;
				method45();
				for(int index = 0; index < 5; index++) {
					anIntArray990[index] = 0;
				}
				for(int index = 0; index < 5; index++) {
					atPlayerActions[index] = null;
					atPlayerArray[index] = false;
				}
				anInt1175 = 0;
				anInt1134 = 0;
				anInt986 = 0;
				anInt1288 = 0;
				anInt924 = 0;
				anInt1188 = 0;
				anInt1155 = 0;
				anInt1226 = 0;
				resetImageProducers2();
				return;
			}
			if(response == 3) {
				loginMessage1 = "Invalid username or password.";
				loginMessage2 = "";
				return;
			}
			if(response == 4) {
				loginMessage1 = "Your account has been disabled.";
				loginMessage2 = "Please check your message-center for details.";
				return;
			}
			if(response == 5) {
				loginMessage1 = "Your account is already logged in.";
				loginMessage2 = "Try again in 60 secs...";
				return;
			}
			if(response == 6) {
				loginMessage1 = "RuneScape has been updated!";
				loginMessage2 = "Please reload this page.";
				return;
			}
			if(response == 7) {
				loginMessage1 = "This world is full.";
				loginMessage2 = "Please use a different world.";
				return;
			}
			if(response == 8) {
				loginMessage1 = "Unable to connect.";
				loginMessage2 = "Login server offline.";
				return;
			}
			if(response == 9) {
				loginMessage1 = "Login limit exceeded.";
				loginMessage2 = "Too many connections from your address.";
				return;
			}
			if(response == 10) {
				loginMessage1 = "Unable to connect.";
				loginMessage2 = "Bad session id.";
				return;
			}
			if(response == 11) {
				loginMessage2 = "Login server rejected session.";
				loginMessage2 = "Please try again.";
				return;
			}
			if(response == 12) {
				loginMessage1 = "You need a members account to login to this world.";
				loginMessage2 = "Please subscribe, or use a different world.";
				return;
			}
			if(response == 13) {
				loginMessage1 = "Could not complete login.";
				loginMessage2 = "Please try using a different world.";
				return;
			}
			if(response == 14) {
				loginMessage1 = "The server is being updated.";
				loginMessage2 = "Please wait 1 minute and try again.";
				return;
			}
			if(response == 15) {
				loggedIn = true;
				out.offset = 0;
				in.offset = 0;
				opCode = -1;
				anInt841 = -1;
				anInt842 = -1;
				anInt843 = -1;
				size = 0;
				anInt1009 = 0;
				anInt1104 = 0;
				menuActionRow = 0;
				menuOpen = false;
				aLong824 = System.currentTimeMillis();
				return;
			}
			if(response == 16) {
				loginMessage1 = "Login attempts exceeded.";
				loginMessage2 = "Please wait 1 minute and try again.";
				return;
			}
			if(response == 17) {
				loginMessage1 = "You are standing in a members-only area.";
				loginMessage2 = "To play on this world move to a free area first";
				return;
			}
			if(response == 20) {
				loginMessage1 = "Invalid loginserver requested";
				loginMessage2 = "Please try using a different world.";
				return;
			}
			if(response == 21) {
				for(int time = socketStream.read(); time >= 0; time--) {
					loginMessage1 = "You have only just left another world";
					loginMessage2 = "Your profile will be transferred in: " + time + " seconds";
					displayTitleScreen(true);
					try {
						Thread.sleep(1000L);
					} catch(Exception _ex) {
					}
				}
				login(username, password, flag, saved);
				return;
			}
			if(response == -1) {
				if(origResponse == 0) {
					if(loginFailures < 2) {
						try {
							Thread.sleep(2000L);
						} catch(Exception _ex) {
						}
						loginFailures++;
						login(username, password, flag, saved);
						return;
					} else {
						loginMessage1 = "No response from loginserver";
						loginMessage2 = "Please wait 1 minute and try again.";
						return;
					}
				} else {
					loginMessage1 = "No response from server";
					loginMessage2 = "Please try using a different world.";
					return;
				}
			} else {
				loginMessage1 = "Unexpected server response: " + response;
				loginMessage2 = "Please try using a different world.";
				return;
			}
		} catch(IOException _ex) {
			loginMessage1 = "";
			loginMessage2 = "";
		}
		loginMessage1 = "Error connecting to server.";
		loginMessage2 = "";
	}

	private boolean doWalkTo(int i, int j, int k, int i1, int j1, int k1,
			int l1, int i2, int j2, boolean flag, int k2)
	{
		byte byte0 = 104;
		byte byte1 = 104;
		for(int l2 = 0; l2 < byte0; l2++)
		{
			for(int i3 = 0; i3 < byte1; i3++)
			{
				anIntArrayArray901[l2][i3] = 0;
				anIntArrayArray825[l2][i3] = 0x5f5e0ff;
			}

		}

		int j3 = j2;
		int k3 = j1;
		anIntArrayArray901[j2][j1] = 99;
		anIntArrayArray825[j2][j1] = 0;
		int l3 = 0;
		int i4 = 0;
		bigX[l3] = j2;
		bigY[l3++] = j1;
		boolean flag1 = false;
		int j4 = bigX.length;
		int ai[][] = aClass11Array1230[floor_level].clipData;
		while(i4 != l3) 
		{
			j3 = bigX[i4];
			k3 = bigY[i4];
			i4 = (i4 + 1) % j4;
			if(j3 == k2 && k3 == i2)
			{
				flag1 = true;
				break;
			}
			if(i1 != 0)
			{
				if((i1 < 5 || i1 == 10) && aClass11Array1230[floor_level].isWalkableA(k2, j3, k3, j, i1 - 1, i2))
				{
					flag1 = true;
					break;
				}
				if(i1 < 10 && aClass11Array1230[floor_level].isWalkableB(k2, i2, k3, i1 - 1, j, j3))
				{
					flag1 = true;
					break;
				}
			}
			if(k1 != 0 && k != 0 && aClass11Array1230[floor_level].isWalkableC(i2, k2, j3, k, l1, k1, k3))
			{
				flag1 = true;
				break;
			}
			int l4 = anIntArrayArray825[j3][k3] + 1;
			if(j3 > 0 && anIntArrayArray901[j3 - 1][k3] == 0 && (ai[j3 - 1][k3] & 0x1280108) == 0)
			{
				bigX[l3] = j3 - 1;
				bigY[l3] = k3;
				l3 = (l3 + 1) % j4;
				anIntArrayArray901[j3 - 1][k3] = 2;
				anIntArrayArray825[j3 - 1][k3] = l4;
			}
			if(j3 < byte0 - 1 && anIntArrayArray901[j3 + 1][k3] == 0 && (ai[j3 + 1][k3] & 0x1280180) == 0)
			{
				bigX[l3] = j3 + 1;
				bigY[l3] = k3;
				l3 = (l3 + 1) % j4;
				anIntArrayArray901[j3 + 1][k3] = 8;
				anIntArrayArray825[j3 + 1][k3] = l4;
			}
			if(k3 > 0 && anIntArrayArray901[j3][k3 - 1] == 0 && (ai[j3][k3 - 1] & 0x1280102) == 0)
			{
				bigX[l3] = j3;
				bigY[l3] = k3 - 1;
				l3 = (l3 + 1) % j4;
				anIntArrayArray901[j3][k3 - 1] = 1;
				anIntArrayArray825[j3][k3 - 1] = l4;
			}
			if(k3 < byte1 - 1 && anIntArrayArray901[j3][k3 + 1] == 0 && (ai[j3][k3 + 1] & 0x1280120) == 0)
			{
				bigX[l3] = j3;
				bigY[l3] = k3 + 1;
				l3 = (l3 + 1) % j4;
				anIntArrayArray901[j3][k3 + 1] = 4;
				anIntArrayArray825[j3][k3 + 1] = l4;
			}
			if(j3 > 0 && k3 > 0 && anIntArrayArray901[j3 - 1][k3 - 1] == 0 && (ai[j3 - 1][k3 - 1] & 0x128010e) == 0 && (ai[j3 - 1][k3] & 0x1280108) == 0 && (ai[j3][k3 - 1] & 0x1280102) == 0)
			{
				bigX[l3] = j3 - 1;
				bigY[l3] = k3 - 1;
				l3 = (l3 + 1) % j4;
				anIntArrayArray901[j3 - 1][k3 - 1] = 3;
				anIntArrayArray825[j3 - 1][k3 - 1] = l4;
			}
			if(j3 < byte0 - 1 && k3 > 0 && anIntArrayArray901[j3 + 1][k3 - 1] == 0 && (ai[j3 + 1][k3 - 1] & 0x1280183) == 0 && (ai[j3 + 1][k3] & 0x1280180) == 0 && (ai[j3][k3 - 1] & 0x1280102) == 0)
			{
				bigX[l3] = j3 + 1;
				bigY[l3] = k3 - 1;
				l3 = (l3 + 1) % j4;
				anIntArrayArray901[j3 + 1][k3 - 1] = 9;
				anIntArrayArray825[j3 + 1][k3 - 1] = l4;
			}
			if(j3 > 0 && k3 < byte1 - 1 && anIntArrayArray901[j3 - 1][k3 + 1] == 0 && (ai[j3 - 1][k3 + 1] & 0x1280138) == 0 && (ai[j3 - 1][k3] & 0x1280108) == 0 && (ai[j3][k3 + 1] & 0x1280120) == 0)
			{
				bigX[l3] = j3 - 1;
				bigY[l3] = k3 + 1;
				l3 = (l3 + 1) % j4;
				anIntArrayArray901[j3 - 1][k3 + 1] = 6;
				anIntArrayArray825[j3 - 1][k3 + 1] = l4;
			}
			if(j3 < byte0 - 1 && k3 < byte1 - 1 && anIntArrayArray901[j3 + 1][k3 + 1] == 0 && (ai[j3 + 1][k3 + 1] & 0x12801e0) == 0 && (ai[j3 + 1][k3] & 0x1280180) == 0 && (ai[j3][k3 + 1] & 0x1280120) == 0)
			{
				bigX[l3] = j3 + 1;
				bigY[l3] = k3 + 1;
				l3 = (l3 + 1) % j4;
				anIntArrayArray901[j3 + 1][k3 + 1] = 12;
				anIntArrayArray825[j3 + 1][k3 + 1] = l4;
			}
		}
		anInt1264 = 0;
		if(!flag1)
		{
			if(flag)
			{
				int i5 = 100;
				for(int k5 = 1; k5 < 2; k5++)
				{
					for(int i6 = k2 - k5; i6 <= k2 + k5; i6++)
					{
						for(int l6 = i2 - k5; l6 <= i2 + k5; l6++)
							if(i6 >= 0 && l6 >= 0 && i6 < 104 && l6 < 104 && anIntArrayArray825[i6][l6] < i5)
							{
								i5 = anIntArrayArray825[i6][l6];
								j3 = i6;
								k3 = l6;
								anInt1264 = 1;
								flag1 = true;
							}

					}

					if(flag1)
						break;
				}

			}
			if(!flag1)
				return false;
		}
		i4 = 0;
		bigX[i4] = j3;
		bigY[i4++] = k3;
		int l5;
		for(int j5 = l5 = anIntArrayArray901[j3][k3]; j3 != j2 || k3 != j1; j5 = anIntArrayArray901[j3][k3])
		{
			if(j5 != l5)
			{
				l5 = j5;
				bigX[i4] = j3;
				bigY[i4++] = k3;
			}
			if((j5 & 2) != 0)
				j3++;
			else
				if((j5 & 8) != 0)
					j3--;
			if((j5 & 1) != 0)
				k3++;
			else
				if((j5 & 4) != 0)
					k3--;
		}
		//	if(cancelWalk) { return i4 > 0; }


		if(i4 > 0)
		{
			int k4 = i4;
			if(k4 > 25)
				k4 = 25;
			i4--;
			int k6 = bigX[i4];
			int i7 = bigY[i4];
			anInt1288 += k4;
			if(anInt1288 >= 92)
			{
				out.putOpCode(36);
				out.putInt(0);
				anInt1288 = 0;
			}
			if(i == 0)
			{
				out.putOpCode(164);
				out.writeWordBigEndian(k4 + k4 + 3);
			}
			if(i == 1)
			{
				out.putOpCode(248);
				out.writeWordBigEndian(k4 + k4 + 3 + 14);
			}
			if(i == 2)
			{
				out.putOpCode(98);
				out.writeWordBigEndian(k4 + k4 + 3);
			}
			out.method433(k6 + baseX);
			destX = bigX[0];
			destY = bigY[0];
			for(int j7 = 1; j7 < k4; j7++)
			{
				i4--;
				out.writeWordBigEndian(bigX[i4] - k6);
				out.writeWordBigEndian(bigY[i4] - i7);
			}

			out.method431(i7 + baseY);
			out.method424(super.keyArray[5] != 1 ? 0 : 1);
			return true;
		}
		return i != 1;
	}

	private void handleNPCMasks(ByteBuffer in) {
		for(int j = 0; j < anInt893; j++) {
			int k = anIntArray894[j];
			NPC npc = npcArray[k];
			int mask = in.getUByte();
			if((mask & 0x10) != 0) {
				int i1 = in.method434();
				if(i1 == 65535)
					i1 = -1;
				int i2 = in.getUByte();
				if(i1 == npc.anim && i1 != -1) {
					int l2 = Sequence.getSeq(i1).anInt365;
					if(l2 == 1) {
						npc.anInt1527 = 0;
						npc.anInt1528 = 0;
						npc.anInt1529 = i2;
						npc.anInt1530 = 0;
					}
					if(l2 == 2)
						npc.anInt1530 = 0;
				} else
					if(i1 == -1 || npc.anim == -1 || Sequence.getSeq(i1).anInt359 >= Sequence.getSeq(npc.anim).anInt359) {
						npc.anim = i1;
						npc.anInt1527 = 0;
						npc.anInt1528 = 0;
						npc.anInt1529 = i2;
						npc.anInt1530 = 0;
						npc.anInt1542 = npc.pathLength;
					}
			}
			if((mask & 8) != 0) {
				int j1 = in.method426();
				int j2 = in.method427();
				npc.updateHitData(j2, j1, currentTime);
				npc.loopCycleStatus = currentTime + 300;
				npc.currentHealth = in.method426();
				npc.maxHealth = in.getUByte();
			}
			if((mask & 0x80) != 0) {
				npc.graphicsId = in.getShort();
				int k1 = in.getInt();
				npc.graphicsHeight = k1 >> 16;
				npc.graphicsDelay = currentTime + (k1 & 0xffff);
				npc.anInt1521 = 0;
				npc.anInt1522 = 0;
				if(npc.graphicsDelay > currentTime)
					npc.anInt1521 = -1;
				if(npc.graphicsId == 65535)
					npc.graphicsId = -1;
			}
			if((mask & 0x20) != 0) {
				npc.interactingEntity = in.getShort();
				if(npc.interactingEntity == 65535)
					npc.interactingEntity = -1;
			}
			if((mask & 1) != 0) {
				npc.textSpoken = in.getString();
				npc.textCycle = 100;
			}
			if((mask & 0x40) != 0) {
				int l1 = in.method427();
				int k2 = in.method428();
				npc.updateHitData(k2, l1, currentTime);
				npc.loopCycleStatus = currentTime + 300;
				npc.currentHealth = in.method428();
				npc.maxHealth = in.method427();
			}
			if((mask & 2) != 0) {
				npc.desc = NPCDef.getDef(in.method436());
				npc.boundDim = npc.desc.aByte68;
				npc.degreesToTurn = npc.desc.anInt79;
				npc.walkAnimIndex = npc.desc.anInt67;
				npc.turn180AnimIndex = npc.desc.anInt58;
				npc.turn90CWAnimIndex = npc.desc.anInt83;
				npc.turn90CCWAnimIndex = npc.desc.anInt55;
				npc.standAnimIndex = npc.desc.anInt77;
			}
			if((mask & 4) != 0) {
				npc.faceX = in.method434();
				npc.faceY = in.method434();
			}
		}
	}

	private void buildAtNPCMenu(NPCDef entityDef, int i, int j, int k)
	{
		if(menuActionRow >= 400)
			return;
		if(entityDef.childrenIDs != null)
			entityDef = entityDef.method161();
		if(entityDef == null)
			return;
		if(!entityDef.aBoolean84)
			return;
		String s = entityDef.name;
		if(entityDef.combatLevel != 0)
			s = s + combatDiffColor(myPlayer.combatLevel, entityDef.combatLevel) + " (level-" + entityDef.combatLevel + ")";
		if(itemSelected == 1)
		{
			menuActionName[menuActionRow] = "Use " + selectedItemName + " with @yel@" + s;
			menuActionID[menuActionRow] = 582;
			menuActionCmd1[menuActionRow] = i;
			menuActionCmd2[menuActionRow] = k;
			menuActionCmd3[menuActionRow] = j;
			menuActionRow++;
			return;
		}
		if(spellSelected == 1)
		{
			if((spellUsableOn & 2) == 2)
			{
				menuActionName[menuActionRow] = spellTooltip + " @yel@" + s;
				menuActionID[menuActionRow] = 413;
				menuActionCmd1[menuActionRow] = i;
				menuActionCmd2[menuActionRow] = k;
				menuActionCmd3[menuActionRow] = j;
				menuActionRow++;
			}
		} else
		{
			if(entityDef.actions != null)
			{
				for(int l = 4; l >= 0; l--)
					if(entityDef.actions[l] != null && !entityDef.actions[l].equalsIgnoreCase("attack"))
					{
						menuActionName[menuActionRow] = entityDef.actions[l] + " @yel@" + s;
						if(l == 0)
							menuActionID[menuActionRow] = 20;
						if(l == 1)
							menuActionID[menuActionRow] = 412;
						if(l == 2)
							menuActionID[menuActionRow] = 225;
						if(l == 3)
							menuActionID[menuActionRow] = 965;
						if(l == 4)
							menuActionID[menuActionRow] = 478;
						menuActionCmd1[menuActionRow] = i;
						menuActionCmd2[menuActionRow] = k;
						menuActionCmd3[menuActionRow] = j;
						menuActionRow++;
					}

			}
			if(entityDef.actions != null)
			{
				for(int i1 = 4; i1 >= 0; i1--)
					if(entityDef.actions[i1] != null && entityDef.actions[i1].equalsIgnoreCase("attack"))
					{
						char c = '\0';
						if(entityDef.combatLevel > myPlayer.combatLevel)
							c = '\u07D0';
						menuActionName[menuActionRow] = entityDef.actions[i1] + " @yel@" + s;
						if(i1 == 0)
							menuActionID[menuActionRow] = 20 + c;
						if(i1 == 1)
							menuActionID[menuActionRow] = 412 + c;
						if(i1 == 2)
							menuActionID[menuActionRow] = 225 + c;
						if(i1 == 3)
							menuActionID[menuActionRow] = 965 + c;
						if(i1 == 4)
							menuActionID[menuActionRow] = 478 + c;
						menuActionCmd1[menuActionRow] = i;
						menuActionCmd2[menuActionRow] = k;
						menuActionCmd3[menuActionRow] = j;
						menuActionRow++;
					}

			}
			menuActionName[menuActionRow] = "Examine @yel@" + s + " @gre@(@whi@" + entityDef.type + "@gre@)";
			menuActionID[menuActionRow] = 1025;
			menuActionCmd1[menuActionRow] = i;
			menuActionCmd2[menuActionRow] = k;
			menuActionCmd3[menuActionRow] = j;
			menuActionRow++;
		}
	}

	private void buildAtPlayerMenu(int i, int j, Player player, int k)
	{
		if(player == myPlayer)
			return;
		if(menuActionRow >= 400)
			return;
		String s;
		if(player.skill == 0)
			s = player.name + combatDiffColor(myPlayer.combatLevel, player.combatLevel) + " (level-" + player.combatLevel + ")";
		else
			s = player.name + " (skill-" + player.skill + ")";
		if(itemSelected == 1)
		{
			menuActionName[menuActionRow] = "Use " + selectedItemName + " with @whi@" + s;
			menuActionID[menuActionRow] = 491;
			menuActionCmd1[menuActionRow] = j;
			menuActionCmd2[menuActionRow] = i;
			menuActionCmd3[menuActionRow] = k;
			menuActionRow++;
		} else
			if(spellSelected == 1)
			{
				if((spellUsableOn & 8) == 8)
				{
					menuActionName[menuActionRow] = spellTooltip + " @whi@" + s;
					menuActionID[menuActionRow] = 365;
					menuActionCmd1[menuActionRow] = j;
					menuActionCmd2[menuActionRow] = i;
					menuActionCmd3[menuActionRow] = k;
					menuActionRow++;
				}
			} else
			{
				for(int l = 4; l >= 0; l--)
					if(atPlayerActions[l] != null)
					{
						menuActionName[menuActionRow] = atPlayerActions[l] + " @whi@" + s;
						char c = '\0';
						if(atPlayerActions[l].equalsIgnoreCase("attack"))
						{
							if(player.combatLevel > myPlayer.combatLevel)
								c = '\u07D0';
							if(myPlayer.team != 0 && player.team != 0)
								if(myPlayer.team == player.team)
									c = '\u07D0';
								else
									c = '\0';
						} else
							if(atPlayerArray[l])
								c = '\u07D0';
						if(l == 0)
							menuActionID[menuActionRow] = 561 + c;
						if(l == 1)
							menuActionID[menuActionRow] = 779 + c;
						if(l == 2)
							menuActionID[menuActionRow] = 27 + c;
						if(l == 3)
							menuActionID[menuActionRow] = 577 + c;
						if(l == 4)
							menuActionID[menuActionRow] = 729 + c;
						menuActionCmd1[menuActionRow] = j;
						menuActionCmd2[menuActionRow] = i;
						menuActionCmd3[menuActionRow] = k;
						menuActionRow++;
					}

			}
		for(int i1 = 0; i1 < menuActionRow; i1++)
			if(menuActionID[i1] == 516)
			{
				menuActionName[i1] = "Walk here @whi@" + s;
				return;
			}

	}

	private void method89(GameObjectSpawnRequest class30_sub1)
	{
		int i = 0;
		int j = -1;
		int k = 0;
		int l = 0;
		if(class30_sub1.anInt1296 == 0)
			i = worldController.getWallObjectUID(class30_sub1.plane, class30_sub1.x, class30_sub1.y);
		if(class30_sub1.anInt1296 == 1)
			i = worldController.getWallDecorationUID(class30_sub1.plane, class30_sub1.x, class30_sub1.y);
		if(class30_sub1.anInt1296 == 2)
			i = worldController.getInteractableObjectUID(class30_sub1.plane, class30_sub1.x, class30_sub1.y);
		if(class30_sub1.anInt1296 == 3)
			i = worldController.getGroundDecorationUID(class30_sub1.plane, class30_sub1.x, class30_sub1.y);
		if(i != 0)
		{
			int i1 = worldController.getIdTagForPosition(class30_sub1.plane, class30_sub1.x, class30_sub1.y, i);
			j = i >> 14 & 0x7fff;
		k = i1 & 0x1f;
		l = i1 >> 6;
		}
		class30_sub1.id = j;
		class30_sub1.type = k;
		class30_sub1.face = l;
	}

	private void method90()
	{
		for(int i = 0; i < anInt1062; i++)
			if(anIntArray1250[i] <= 0)
			{
				boolean flag1 = false;
				try
				{
					if(anIntArray1207[i] == anInt874 && anIntArray1241[i] == anInt1289)
					{
						if(!replayWave())
							flag1 = true;
					} else
					{
						ByteBuffer stream = Sounds.getSoundBuffer(anIntArray1241[i], anIntArray1207[i]);
						if(System.currentTimeMillis() + (long)(stream.offset / 22) > aLong1172 + (long)(anInt1257 / 22))
						{
							anInt1257 = stream.offset;
							aLong1172 = System.currentTimeMillis();
							if(saveWave(stream.buffer, stream.offset))
							{
								anInt874 = anIntArray1207[i];
								anInt1289 = anIntArray1241[i];
							} else
							{
								flag1 = true;
							}
						}
					}
				}
				catch(Exception exception) { }
				if(!flag1 || anIntArray1250[i] == -5)
				{
					anInt1062--;
					for(int j = i; j < anInt1062; j++)
					{
						anIntArray1207[j] = anIntArray1207[j + 1];
						anIntArray1241[j] = anIntArray1241[j + 1];
						anIntArray1250[j] = anIntArray1250[j + 1];
					}

					i--;
				} else
				{
					anIntArray1250[i] = -5;
				}
			} else
			{
				anIntArray1250[i]--;
			}

		if(prevSong > 0)
		{
			prevSong -= 20;
			if(prevSong < 0)
				prevSong = 0;
			if(prevSong == 0 && musicEnabled && !lowMem)
			{
				nextSong = currentSong;
				songChanging = true;
				onDemandFetcher.method558(2, nextSong);
			}
		}
	}

	void startUp() {
		displayProgress("Starting up", 20);
		if(signlink.sunjava)
			super.minDelay = 5;
		if(aBoolean993) {
			//		   rsAlreadyLoaded = true;
			//		   return;
		}
		aBoolean993 = true;
		boolean flag = true;
		if(!flag) {
			genericLoadingError = true;
			return;
		}
		if(signlink.cache_dat != null) {
			for(int i = 0; i < 5; i++)
				jagCache[i] = new ResourceCache(signlink.cache_dat, signlink.cache_idx[i], i + 1);
		}
		try {
			if (Constants.UPDATE_SERVER_ENABLED) {
				connect();
			}
			titleArchive = streamLoaderForName(1, "title screen", "title", expectedCRCs[1], 25);
			small = new RSFont(false, "p11_full", titleArchive);
			regular = new RSFont(false, "p12_full", titleArchive);
			bold = new RSFont(false, "b12_full", titleArchive);
			fancy = new RSFont(true, "q8_full", titleArchive);
			//drawLogo();
			loadTitleScreen();
			JagexArchive config = streamLoaderForName(2, "config", "config", expectedCRCs[2], 30);
			JagexArchive interfaces = streamLoaderForName(3, "interface", "interface", expectedCRCs[3], 35);
			JagexArchive media = streamLoaderForName(4, "2d graphics", "media", expectedCRCs[4], 40);
			JagexArchive textures = streamLoaderForName(6, "textures", "textures", expectedCRCs[6], 45);
			JagexArchive chat = streamLoaderForName(7, "chat system", "wordenc", expectedCRCs[7], 50);
			JagexArchive sounds = streamLoaderForName(8, "sound effects", "sounds", expectedCRCs[8], 55);
			byteGroundArray = new byte[4][104][104];
			intGroundArray = new int[4][105][105];
			worldController = new SceneGraph(intGroundArray);
			for(int j = 0; j < 4; j++)
				aClass11Array1230[j] = new TileSetting();

			aClass30_Sub2_Sub1_Sub1_1263 = new RSImage(512, 512);
			JagexArchive streamLoader_6 = streamLoaderForName(5, "update list", "versionlist", expectedCRCs[5], 60);
			displayProgress("Connecting to update server", 60);
			onDemandFetcher = new ResourceProvider();
			onDemandFetcher.start(streamLoader_6, this);
			FrameHeader.method528(onDemandFetcher.getAnimCount());
			Model.method459(onDemandFetcher.getVersionCount(0), onDemandFetcher);
			if(!lowMem) {
				nextSong = 0;
				try {
					nextSong = Integer.parseInt(getParameter("music"));
				}
				catch(Exception _ex) { }
				songChanging = true;
				onDemandFetcher.method558(2, nextSong);
				while(onDemandFetcher.getNodeCount() > 0) {
					processOnDemandQueue();
					try {
						Thread.sleep(100L);
					}
					catch(Exception _ex) { }
					if(onDemandFetcher.anInt1349 > 3) {
						loadError();
						return;
					}
				}
			}
			displayProgress("Requesting animations", 65);
			int k = onDemandFetcher.getVersionCount(1);
			for(int i1 = 0; i1 < k; i1++)
				onDemandFetcher.method558(1, i1);

			while(onDemandFetcher.getNodeCount() > 0) {
				int j1 = k - onDemandFetcher.getNodeCount();
				if(j1 > 0)
					displayProgress("Loading animations - " + (j1 * 100) / k + "%", 65);
				processOnDemandQueue();
				try {
					Thread.sleep(100L);
				}
				catch(Exception _ex) { }
				if(onDemandFetcher.anInt1349 > 3) {
					loadError();
					return;
				}
			}
			displayProgress("Requesting models", 70);
			k = onDemandFetcher.getVersionCount(0);
			for(int k1 = 0; k1 < k; k1++) {
				int l1 = onDemandFetcher.getModelIndex(k1);
				if((l1 & 1) != 0)
					onDemandFetcher.method558(0, k1);
			}

			k = onDemandFetcher.getNodeCount();
			while(onDemandFetcher.getNodeCount() > 0)
			{
				int i2 = k - onDemandFetcher.getNodeCount();
				if(i2 > 0)
					displayProgress("Loading models - " + (i2 * 100) / k + "%", 70);
				processOnDemandQueue();
				try
				{
					Thread.sleep(100L);
				}
				catch(Exception _ex) { }
			}
			if(jagCache[0] != null)
			{
				displayProgress("Requesting maps", 75);
				onDemandFetcher.method558(3, onDemandFetcher.method562(0, 48, 47));
				onDemandFetcher.method558(3, onDemandFetcher.method562(1, 48, 47));
				onDemandFetcher.method558(3, onDemandFetcher.method562(0, 48, 48));
				onDemandFetcher.method558(3, onDemandFetcher.method562(1, 48, 48));
				onDemandFetcher.method558(3, onDemandFetcher.method562(0, 48, 49));
				onDemandFetcher.method558(3, onDemandFetcher.method562(1, 48, 49));
				onDemandFetcher.method558(3, onDemandFetcher.method562(0, 47, 47));
				onDemandFetcher.method558(3, onDemandFetcher.method562(1, 47, 47));
				onDemandFetcher.method558(3, onDemandFetcher.method562(0, 47, 48));
				onDemandFetcher.method558(3, onDemandFetcher.method562(1, 47, 48));
				onDemandFetcher.method558(3, onDemandFetcher.method562(0, 148, 48));
				onDemandFetcher.method558(3, onDemandFetcher.method562(1, 148, 48));
				k = onDemandFetcher.getNodeCount();
				while(onDemandFetcher.getNodeCount() > 0)
				{
					int j2 = k - onDemandFetcher.getNodeCount();
					if(j2 > 0)
						displayProgress("Loading maps - " + (j2 * 100) / k + "%", 75);
					processOnDemandQueue();
					try
					{
						Thread.sleep(100L);
					}
					catch(Exception _ex) { }
				}
			}
			k = onDemandFetcher.getVersionCount(0);
			for(int k2 = 0; k2 < k; k2++)
			{
				int l2 = onDemandFetcher.getModelIndex(k2);
				byte byte0 = 0;
				if((l2 & 8) != 0)
					byte0 = 10;
				else
					if((l2 & 0x20) != 0)
						byte0 = 9;
					else
						if((l2 & 0x10) != 0)
							byte0 = 8;
						else
							if((l2 & 0x40) != 0)
								byte0 = 7;
							else
								if((l2 & 0x80) != 0)
									byte0 = 6;
								else
									if((l2 & 2) != 0)
										byte0 = 5;
									else
										if((l2 & 4) != 0)
											byte0 = 4;
				if((l2 & 1) != 0)
					byte0 = 3;
				if(byte0 != 0)
					onDemandFetcher.method563(byte0, 0, k2);
			}

			onDemandFetcher.method554(isMembers);
			if(!lowMem)
			{
				int l = onDemandFetcher.getVersionCount(2);
				for(int i3 = 1; i3 < l; i3++)
					if(onDemandFetcher.method569(i3))
						onDemandFetcher.method563((byte)1, 2, i3);

			}
			displayProgress("Unpacking media", 80);
			button = new RSImage("titlelogin");
			button_hover = new RSImage("titlelogin hover");
			field = new RSImage("titlefield");
			field_hover = new RSImage("titlefield hover");
			invBack = new IndexedImage(media, "invback", 0);
			chatBack = new IndexedImage(media, "chatback", 0);
			mapBack = new IndexedImage(media, "mapback", 0);
			backBase1 = new IndexedImage(media, "backbase1", 0);
			backBase2 = new IndexedImage(media, "backbase2", 0);
			backHmid1 = new IndexedImage(media, "backhmid1", 0);
			for(int j3 = 0; j3 < 13; j3++)
				sideIcons[j3] = new IndexedImage(media, "sideicons", j3);

			compass = new RSImage(media, "compass", 0);
			mapEdge = new RSImage(media, "mapedge", 0);
			mapEdge.trim();
			try
			{
				for(int k3 = 0; k3 < 100; k3++)
					mapScenes[k3] = new IndexedImage(media, "mapscene", k3);

			}
			catch(Exception _ex) { }
			try
			{
				for(int l3 = 0; l3 < 100; l3++)
					mapFunctions[l3] = new RSImage(media, "mapfunction", l3);

			}
			catch(Exception _ex) { }
			try
			{
				for(int i4 = 0; i4 < 20; i4++)
					hitMarks[i4] = new RSImage(media, "hitmarks", i4);

			}
			catch(Exception _ex) { }
			try {
				for(int index = 0; index < 20; index++)
					headIcons[index] = new RSImage(media, "headicons_prayer", index);

			}
			catch(Exception _ex) { }
			mapFlag = new RSImage(media, "mapmarker", 0);
			mapMarker = new RSImage(media, "mapmarker", 1);
			for(int k4 = 0; k4 < 8; k4++)
				crosses[k4] = new RSImage(media, "cross", k4);

			mapDotItem = new RSImage(media, "mapdots", 0);
			mapDotNPC = new RSImage(media, "mapdots", 1);
			mapDotPlayer = new RSImage(media, "mapdots", 2);
			mapDotFriend = new RSImage(media, "mapdots", 3);
			mapDotTeam = new RSImage(media, "mapdots", 4);
			scrollBar1 = new IndexedImage(media, "scrollbar", 0);
			scrollBar2 = new IndexedImage(media, "scrollbar", 1);
			redStone1 = new IndexedImage(media, "redstone1", 0);
			redStone2 = new IndexedImage(media, "redstone2", 0);
			redStone3 = new IndexedImage(media, "redstone3", 0);
			redStone1_2 = new IndexedImage(media, "redstone1", 0);
			redStone1_2.method358();
			redStone2_2 = new IndexedImage(media, "redstone2", 0);
			redStone2_2.method358();
			redStone1_3 = new IndexedImage(media, "redstone1", 0);
			redStone1_3.method359();
			redStone2_3 = new IndexedImage(media, "redstone2", 0);
			redStone2_3.method359();
			redStone3_2 = new IndexedImage(media, "redstone3", 0);
			redStone3_2.method359();
			redStone1_4 = new IndexedImage(media, "redstone1", 0);
			redStone1_4.method358();
			redStone1_4.method359();
			redStone2_4 = new IndexedImage(media, "redstone2", 0);
			redStone2_4.method358();
			redStone2_4.method359();
			for(int l4 = 0; l4 < 2; l4++)
				modIcons[l4] = new IndexedImage(media, "mod_icons", l4);

			RSImage sprite = new RSImage(media, "backleft1", 0);
			backLeftIP1 = new RSImageProducer(sprite.myWidth, sprite.myHeight, getGameComponent());
			sprite.drawInverse(0, 0);
			sprite = new RSImage(media, "backleft2", 0);
			backLeftIP2 = new RSImageProducer(sprite.myWidth, sprite.myHeight, getGameComponent());
			sprite.drawInverse(0, 0);
			sprite = new RSImage(media, "backright1", 0);
			backRightIP1 = new RSImageProducer(sprite.myWidth, sprite.myHeight, getGameComponent());
			sprite.drawInverse(0, 0);
			sprite = new RSImage(media, "backright2", 0);
			backRightIP2 = new RSImageProducer(sprite.myWidth, sprite.myHeight, getGameComponent());
			sprite.drawInverse(0, 0);
			sprite = new RSImage(media, "backtop1", 0);
			backTopIP1 = new RSImageProducer(sprite.myWidth, sprite.myHeight, getGameComponent());
			sprite.drawInverse(0, 0);
			sprite = new RSImage(media, "backvmid1", 0);
			backVmidIP1 = new RSImageProducer(sprite.myWidth, sprite.myHeight, getGameComponent());
			sprite.drawInverse(0, 0);
			sprite = new RSImage(media, "backvmid2", 0);
			backVmidIP2 = new RSImageProducer(sprite.myWidth, sprite.myHeight, getGameComponent());
			sprite.drawInverse(0, 0);
			sprite = new RSImage(media, "backvmid3", 0);
			backVmidIP3 = new RSImageProducer(sprite.myWidth, sprite.myHeight, getGameComponent());
			sprite.drawInverse(0, 0);
			sprite = new RSImage(media, "backhmid2", 0);
			backVmidIP2_2 = new RSImageProducer(sprite.myWidth, sprite.myHeight, getGameComponent());
			sprite.drawInverse(0, 0);
			int i5 = (int)(Math.random() * 21D) - 10;
			int j5 = (int)(Math.random() * 21D) - 10;
			int k5 = (int)(Math.random() * 21D) - 10;
			int l5 = (int)(Math.random() * 41D) - 20;
			for(int i6 = 0; i6 < 100; i6++)
			{
				if(mapFunctions[i6] != null)
					mapFunctions[i6].adjustColors(i5 + l5, j5 + l5, k5 + l5);
				if(mapScenes[i6] != null)
					mapScenes[i6].method360(i5 + l5, j5 + l5, k5 + l5);
			}

			displayProgress("Unpacking textures", 83);
			Rasterizer.unpack(textures);
			Rasterizer.calculatePalette(0.80000000000000004D);
			Rasterizer.resetTextures();
			displayProgress("Unpacking config", 86);
			Sequence.unpackConfig(config);
			ObjectDef.unpackConfig(config);
			Floor.unpackConfig(config);
			ItemDef.unpackConfig(config);
			NPCDef.unpackConfig(config);
			IdentityKit.unpackConfig(config);
			SpotAnim.unpackConfig(config);
			Varp.unpackConfig(config);
			VarBit.unpackConfig(config);
			ItemDef.isMembers = isMembers;
			if(!lowMem)
			{
				displayProgress("Unpacking sounds", 90);
				byte abyte0[] = sounds.getData("sounds.dat");
				ByteBuffer stream = new ByteBuffer(abyte0);
				Sounds.unpack(stream);
			}
			displayProgress("Unpacking interfaces", 95);
			RSFont aclass30_sub2_sub1_sub4s[] = {
					small, regular, bold, fancy
			};
			RSInterface.unpack(interfaces, aclass30_sub2_sub1_sub4s, media);
			displayProgress("Preparing game engine", 100);
			for(int j6 = 0; j6 < 33; j6++)
			{
				int k6 = 999;
				int i7 = 0;
				for(int k7 = 0; k7 < 34; k7++)
				{
					if(mapBack.myPixels[k7 + j6 * mapBack.myWidth] == 0)
					{
						if(k6 == 999)
							k6 = k7;
						continue;
					}
					if(k6 == 999)
						continue;
					i7 = k7;
					break;
				}

				anIntArray968[j6] = k6;
				anIntArray1057[j6] = i7 - k6;
			}

			for(int l6 = 5; l6 < 156; l6++)
			{
				int j7 = 999;
				int l7 = 0;
				for(int j8 = 25; j8 < 172; j8++)
				{
					if(mapBack.myPixels[j8 + l6 * mapBack.myWidth] == 0 && (j8 > 34 || l6 > 34))
					{
						if(j7 == 999)
							j7 = j8;
						continue;
					}
					if(j7 == 999)
						continue;
					l7 = j8;
					break;
				}

				anIntArray1052[l6 - 5] = j7 - 25;
				anIntArray1229[l6 - 5] = l7 - j7;
			}
			Rasterizer.setBounds(getClientWidth(), getClientHeight());
            fullScreenTextureArray = Rasterizer.lineOffsets;
			Rasterizer.setBounds(479, 96);
			chatAreaTextureArray = Rasterizer.lineOffsets;
			Rasterizer.setBounds(190, 261);
			tabAreaTextureArray = Rasterizer.lineOffsets;
			Rasterizer.setBounds(512, 334);
			gameAreaTextureArray = Rasterizer.lineOffsets;
			int ai[] = new int[9];
			for(int i8 = 0; i8 < 9; i8++) {
				int k8 = 128 + i8 * 32 + 15;
				int l8 = 600 + k8 * 3;
				int i9 = Rasterizer.SINE[k8];
				ai[i8] = l8 * i9 >> 16;
			}
			SceneGraph.setupViewport(500, 800, 512, 334, ai);
			Censor.loadConfig(chat);
			mouseDetection = new MouseDetection(this);
			startRunnable(mouseDetection, 10);
			ObjectOnTile.clientInstance = this;
			ObjectDef.clientInstance = this;
			NPCDef.clientInstance = this;
			Accounts.read();
			return;
		} catch(Exception exception) {
			signlink.reportError("loaderror " + aString1049 + " " + anInt1079);
			exception.printStackTrace();
		}
		loadingError = true;
	}

	private void method91(ByteBuffer stream, int i)
	{
		while(stream.pos + 10 < i * 8)
		{
			int j = stream.getBits(11);
			if(j == 2047)
				break;
			if(playerArray[j] == null)
			{
				playerArray[j] = new Player();
				if(aStreamArray895s[j] != null)
					playerArray[j].updatePlayer(aStreamArray895s[j]);
			}
			playerIndices[playerCount++] = j;
			Player player = playerArray[j];
			player.time = currentTime;
			int k = stream.getBits(1);
			if(k == 1)
				anIntArray894[anInt893++] = j;
			int l = stream.getBits(1);
			int i1 = stream.getBits(5);
			if(i1 > 15)
				i1 -= 32;
			int j1 = stream.getBits(5);
			if(j1 > 15)
				j1 -= 32;
			player.setPos(myPlayer.pathX[0] + j1, myPlayer.pathY[0] + i1, l == 1);
		}
		stream.finishBitAccess();
	}

	private void processMainScreenClick()
	{
		if(anInt1021 != 0)
			return;
		if(super.clickMode3 == 1)
		{
			int i = super.saveClickX - 25 - 550;
			int j = super.saveClickY - 5 - 4;
			if(i >= 0 && j >= 0 && i < 146 && j < 151)
			{
				i -= 73;
				j -= 75;
				int k = minimapInt1 + minimapInt2 & 0x7ff;
				int i1 = Rasterizer.SINE[k];
				int j1 = Rasterizer.COSINE[k];
				i1 = i1 * (minimapInt3 + 256) >> 8;
			j1 = j1 * (minimapInt3 + 256) >> 8;
			int k1 = j * i1 + i * j1 >> 11;
				int l1 = j * j1 - i * i1 >> 11;
				int i2 = myPlayer.currentX + k1 >> 7;
				int j2 = myPlayer.currentY - l1 >> 7;
				boolean flag1 = doWalkTo(1, 0, 0, 0, myPlayer.pathY[0], 0, 0, j2, myPlayer.pathX[0], true, i2);
				if(flag1)
				{
					out.writeWordBigEndian(i);
					out.writeWordBigEndian(j);
					out.putShort(minimapInt1);
					out.writeWordBigEndian(57);
					out.writeWordBigEndian(minimapInt2);
					out.writeWordBigEndian(minimapInt3);
					out.writeWordBigEndian(89);
					out.putShort(myPlayer.currentX);
					out.putShort(myPlayer.currentY);
					out.writeWordBigEndian(anInt1264);
					out.writeWordBigEndian(63);
				}
			}
			lastAction++;
			if(lastAction > 1151) {
				lastAction = 0;
				out.putOpCode(246);
				out.writeWordBigEndian(0);
				int l = out.offset;
				if((int)(Math.random() * 2D) == 0) {
					out.writeWordBigEndian(101);
				}
				out.writeWordBigEndian(197);
				out.putShort((int)(Math.random() * 65536D));
				out.writeWordBigEndian((int)(Math.random() * 256D));
				out.writeWordBigEndian(67);
				out.putShort(14214);
				if((int)(Math.random() * 2D) == 0) {
					out.putShort(29487);
				}
				out.putShort((int)(Math.random() * 65536D));
				if((int)(Math.random() * 2D) == 0) {
					out.writeWordBigEndian(220);
				}
				out.writeWordBigEndian(180);
				out.putBytes(out.offset - l);
			}
		}
	}

	private String interfaceIntToString(int j)
	{
		if(j < 0x3b9ac9ff)
			return String.valueOf(j);
		else
			return "*";
	}

	private void showErrorScreen()
	{
		Graphics g = getGameComponent().getGraphics();
		g.setColor(Color.black);
		g.fillRect(0, 0, 765, 503);
		method4(1);
		if(loadingError)
		{
			aBoolean831 = false;
			g.setFont(new Font("Helvetica", 1, 16));
			g.setColor(Color.yellow);
			int k = 35;
			g.drawString("Sorry, an error has occured whilst loading RuneScape", 30, k);
			k += 50;
			g.setColor(Color.white);
			g.drawString("To fix this try the following (in order):", 30, k);
			k += 50;
			g.setColor(Color.white);
			g.setFont(new Font("Helvetica", 1, 12));
			g.drawString("1: Try closing ALL open web-browser windows, and reloading", 30, k);
			k += 30;
			g.drawString("2: Try clearing your web-browsers cache from tools->internet options", 30, k);
			k += 30;
			g.drawString("3: Try using a different game-world", 30, k);
			k += 30;
			g.drawString("4: Try rebooting your computer", 30, k);
			k += 30;
			g.drawString("5: Try selecting a different version of Java from the play-game menu", 30, k);
		}
		if(genericLoadingError)
		{
			aBoolean831 = false;
			g.setFont(new Font("Helvetica", 1, 20));
			g.setColor(Color.white);
			g.drawString("Error - unable to load game!", 50, 50);
			g.drawString("To play RuneScape make sure you play from", 50, 100);
			g.drawString("http://www.runescape.com", 50, 150);
		}
		if(rsAlreadyLoaded)
		{
			aBoolean831 = false;
			g.setColor(Color.yellow);
			int l = 35;
			g.drawString("Error a copy of RuneScape already appears to be loaded", 30, l);
			l += 50;
			g.setColor(Color.white);
			g.drawString("To fix this try the following (in order):", 30, l);
			l += 50;
			g.setColor(Color.white);
			g.setFont(new Font("Helvetica", 1, 12));
			g.drawString("1: Try closing ALL open web-browser windows, and reloading", 30, l);
			l += 30;
			g.drawString("2: Try rebooting your computer, and reloading", 30, l);
			l += 30;
		}
	}

	public URL getCodeBase() {
		if(signlink.mainapp != null)
			return signlink.mainapp.getCodeBase();
		try {
			if(super.mainFrame != null)
				return new URL("http://" + Constants.HOST_ADDRESS + ":" + (80 + portOff));
		}
		catch(Exception _ex) { }
		return super.getCodeBase();
	}

	private void method95()
	{
		for(int j = 0; j < npcCount; j++)
		{
			int k = npcIndices[j];
			NPC npc = npcArray[k];
			if(npc != null)
				method96(npc);
		}
	}

	private void method96(Entity entity)
	{
		if(entity.currentX < 128 || entity.currentY < 128 || entity.currentX >= 13184 || entity.currentY >= 13184)
		{
			entity.anim = -1;
			entity.graphicsId = -1;
			entity.anInt1547 = 0;
			entity.anInt1548 = 0;
			entity.currentX = entity.pathX[0] * 128 + entity.boundDim * 64;
			entity.currentY = entity.pathY[0] * 128 + entity.boundDim * 64;
			entity.method446();
		}
		if(entity == myPlayer && (entity.currentX < 1536 || entity.currentY < 1536 || entity.currentX >= 11776 || entity.currentY >= 11776))
		{
			entity.anim = -1;
			entity.graphicsId = -1;
			entity.anInt1547 = 0;
			entity.anInt1548 = 0;
			entity.currentX = entity.pathX[0] * 128 + entity.boundDim * 64;
			entity.currentY = entity.pathY[0] * 128 + entity.boundDim * 64;
			entity.method446();
		}
		if(entity.anInt1547 > currentTime)
			method97(entity);
		else
			if(entity.anInt1548 >= currentTime)
				method98(entity);
			else
				method99(entity);
		method100(entity);
		method101(entity);
	}

	private void method97(Entity entity)
	{
		int i = entity.anInt1547 - currentTime;
		int j = entity.anInt1543 * 128 + entity.boundDim * 64;
		int k = entity.anInt1545 * 128 + entity.boundDim * 64;
		entity.currentX += (j - entity.currentX) / i;
		entity.currentY += (k - entity.currentY) / i;
		entity.anInt1503 = 0;
		if(entity.turnInfo == 0)
			entity.turnDirection = 1024;
		if(entity.turnInfo == 1)
			entity.turnDirection = 1536;
		if(entity.turnInfo == 2)
			entity.turnDirection = 0;
		if(entity.turnInfo == 3)
			entity.turnDirection = 512;
	}

	private void method98(Entity entity)
	{
		if(entity.anInt1548 == currentTime || entity.anim == -1 || entity.anInt1529 != 0 || entity.anInt1528 + 1 > Sequence.getSeq(entity.anim).method258(entity.anInt1527))
		{
			int i = entity.anInt1548 - entity.anInt1547;
			int j = currentTime - entity.anInt1547;
			int k = entity.anInt1543 * 128 + entity.boundDim * 64;
			int l = entity.anInt1545 * 128 + entity.boundDim * 64;
			int i1 = entity.anInt1544 * 128 + entity.boundDim * 64;
			int j1 = entity.anInt1546 * 128 + entity.boundDim * 64;
			entity.currentX = (k * (i - j) + i1 * j) / i;
			entity.currentY = (l * (i - j) + j1 * j) / i;
		}
		entity.anInt1503 = 0;
		if(entity.turnInfo == 0)
			entity.turnDirection = 1024;
		if(entity.turnInfo == 1)
			entity.turnDirection = 1536;
		if(entity.turnInfo == 2)
			entity.turnDirection = 0;
		if(entity.turnInfo == 3)
			entity.turnDirection = 512;
		entity.currentRotation = entity.turnDirection;
	}

	private void method99(Entity entity)
	{
		entity.anInt1517 = entity.standAnimIndex;
		if(entity.pathLength == 0)
		{
			entity.anInt1503 = 0;
			return;
		}
		if(entity.anim != -1 && entity.anInt1529 == 0)
		{
			Sequence animation = Sequence.getSeq(entity.anim);
			if(entity.anInt1542 > 0 && animation.anInt363 == 0)
			{
				entity.anInt1503++;
				return;
			}
			if(entity.anInt1542 <= 0 && animation.priority == 0)
			{
				entity.anInt1503++;
				return;
			}
		}
		int i = entity.currentX;
		int j = entity.currentY;
		int k = entity.pathX[entity.pathLength - 1] * 128 + entity.boundDim * 64;
		int l = entity.pathY[entity.pathLength - 1] * 128 + entity.boundDim * 64;
		if(k - i > 256 || k - i < -256 || l - j > 256 || l - j < -256)
		{
			entity.currentX = k;
			entity.currentY = l;
			return;
		}
		if(i < k)
		{
			if(j < l)
				entity.turnDirection = 1280;
			else
				if(j > l)
					entity.turnDirection = 1792;
				else
					entity.turnDirection = 1536;
		} else
			if(i > k)
			{
				if(j < l)
					entity.turnDirection = 768;
				else
					if(j > l)
						entity.turnDirection = 256;
					else
						entity.turnDirection = 512;
			} else
				if(j < l)
					entity.turnDirection = 1024;
				else
					entity.turnDirection = 0;
		int i1 = entity.turnDirection - entity.currentRotation & 0x7ff;
		if(i1 > 1024)
			i1 -= 2048;
		int j1 = entity.turn180AnimIndex;
		if(i1 >= -256 && i1 <= 256)
			j1 = entity.walkAnimIndex;
		else
			if(i1 >= 256 && i1 < 768)
				j1 = entity.turn90CCWAnimIndex;
			else
				if(i1 >= -768 && i1 <= -256)
					j1 = entity.turn90CWAnimIndex;
		if(j1 == -1)
			j1 = entity.walkAnimIndex;
		entity.anInt1517 = j1;
		int k1 = 4;
		if(entity.currentRotation != entity.turnDirection && entity.interactingEntity == -1 && entity.degreesToTurn != 0)
			k1 = 2;
		if(entity.pathLength > 2)
			k1 = 6;
		if(entity.pathLength > 3)
			k1 = 8;
		if(entity.anInt1503 > 0 && entity.pathLength > 1)
		{
			k1 = 8;
			entity.anInt1503--;
		}
		if(entity.pathRun[entity.pathLength - 1])
			k1 <<= 1;
		if(k1 >= 8 && entity.anInt1517 == entity.walkAnimIndex && entity.runAnimIndex != -1)
			entity.anInt1517 = entity.runAnimIndex;
		if(i < k)
		{
			entity.currentX += k1;
			if(entity.currentX > k)
				entity.currentX = k;
		} else
			if(i > k)
			{
				entity.currentX -= k1;
				if(entity.currentX < k)
					entity.currentX = k;
			}
		if(j < l)
		{
			entity.currentY += k1;
			if(entity.currentY > l)
				entity.currentY = l;
		} else
			if(j > l)
			{
				entity.currentY -= k1;
				if(entity.currentY < l)
					entity.currentY = l;
			}
		if(entity.currentX == k && entity.currentY == l)
		{
			entity.pathLength--;
			if(entity.anInt1542 > 0)
				entity.anInt1542--;
		}
	}

	private void method100(Entity entity)
	{
		if(entity.degreesToTurn == 0)
			return;
		if(entity.interactingEntity != -1 && entity.interactingEntity < 32768)
		{
			NPC npc = npcArray[entity.interactingEntity];
			if(npc != null)
			{
				int i1 = entity.currentX - npc.currentX;
				int k1 = entity.currentY - npc.currentY;
				if(i1 != 0 || k1 != 0)
					entity.turnDirection = (int)(Math.atan2(i1, k1) * 325.94900000000001D) & 0x7ff;
			}
		}
		if(entity.interactingEntity >= 32768)
		{
			int j = entity.interactingEntity - 32768;
			if(j == unknownInt10)
				j = myPlayerIndex;
			Player player = playerArray[j];
			if(player != null)
			{
				int l1 = entity.currentX - player.currentX;
				int i2 = entity.currentY - player.currentY;
				if(l1 != 0 || i2 != 0)
					entity.turnDirection = (int)(Math.atan2(l1, i2) * 325.94900000000001D) & 0x7ff;
			}
		}
		if((entity.faceX != 0 || entity.faceY != 0) && (entity.pathLength == 0 || entity.anInt1503 > 0))
		{
			int k = entity.currentX - (entity.faceX - baseX - baseX) * 64;
			int j1 = entity.currentY - (entity.faceY - baseY - baseY) * 64;
			if(k != 0 || j1 != 0)
				entity.turnDirection = (int)(Math.atan2(k, j1) * 325.94900000000001D) & 0x7ff;
			entity.faceX = 0;
			entity.faceY = 0;
		}
		int l = entity.turnDirection - entity.currentRotation & 0x7ff;
		if(l != 0)
		{
			if(l < entity.degreesToTurn || l > 2048 - entity.degreesToTurn)
				entity.currentRotation = entity.turnDirection;
			else
				if(l > 1024)
					entity.currentRotation -= entity.degreesToTurn;
				else
					entity.currentRotation += entity.degreesToTurn;
			entity.currentRotation &= 0x7ff;
			if(entity.anInt1517 == entity.standAnimIndex && entity.currentRotation != entity.turnDirection)
			{
				if(entity.standTurnAnimIndex != -1)
				{
					entity.anInt1517 = entity.standTurnAnimIndex;
					return;
				}
				entity.anInt1517 = entity.walkAnimIndex;
			}
		}
	}

	private void method101(Entity entity)
	{
		entity.aBoolean1541 = false;
		if(entity.anInt1517 != -1)
		{
			Sequence animation = Sequence.getSeq(entity.anInt1517);
			entity.anInt1519++;
			if(entity.anInt1518 < animation.totalFrames && entity.anInt1519 > animation.method258(entity.anInt1518))
			{
				entity.anInt1519 = 0;
				entity.anInt1518++;
			}
			if(entity.anInt1518 >= animation.totalFrames)
			{
				entity.anInt1519 = 0;
				entity.anInt1518 = 0;
			}
		}
		if(entity.graphicsId != -1 && currentTime >= entity.graphicsDelay)
		{
			if(entity.anInt1521 < 0)
				entity.anInt1521 = 0;
			Sequence animation_1 = SpotAnim.cache[entity.graphicsId].sequence;
			for(entity.anInt1522++; entity.anInt1521 < animation_1.totalFrames && entity.anInt1522 > animation_1.method258(entity.anInt1521); entity.anInt1521++)
				entity.anInt1522 -= animation_1.method258(entity.anInt1521);

			if(entity.anInt1521 >= animation_1.totalFrames && (entity.anInt1521 < 0 || entity.anInt1521 >= animation_1.totalFrames))
				entity.graphicsId = -1;
		}
		if(entity.anim != -1 && entity.anInt1529 <= 1)
		{
			Sequence animation_2 = Sequence.getSeq(entity.anim);
			if(animation_2.anInt363 == 1 && entity.anInt1542 > 0 && entity.anInt1547 <= currentTime && entity.anInt1548 < currentTime)
			{
				entity.anInt1529 = 1;
				return;
			}
		}
		if(entity.anim != -1 && entity.anInt1529 == 0)
		{
			Sequence animation_3 = Sequence.getSeq(entity.anim);
			for(entity.anInt1528++; entity.anInt1527 < animation_3.totalFrames && entity.anInt1528 > animation_3.method258(entity.anInt1527); entity.anInt1527++)
				entity.anInt1528 -= animation_3.method258(entity.anInt1527);

			if(entity.anInt1527 >= animation_3.totalFrames)
			{
				entity.anInt1527 -= animation_3.frameStep;
				entity.anInt1530++;
				if(entity.anInt1530 >= animation_3.anInt362)
					entity.anim = -1;
				if(entity.anInt1527 < 0 || entity.anInt1527 >= animation_3.totalFrames)
					entity.anim = -1;
			}
			entity.aBoolean1541 = animation_3.aBoolean358;
		}
		if(entity.anInt1529 > 0)
			entity.anInt1529--;
	}

	private void drawGameScreen() {
		if (fullscreenInterfaceID != -1 && (loadingStage == 2 || super.fullGameScreen != null)) {
	        if (loadingStage == 2) {
	            method119(anInt945, fullscreenInterfaceID);
	            if (openInterfaceID != -1) {
	                method119(anInt945, openInterfaceID);
	            }
	            anInt945 = 0;
	            resetAllImageProducers();
	            super.fullGameScreen.initDrawingArea();
	            Rasterizer.lineOffsets = fullScreenTextureArray;
	            RSDrawingArea.setAllPixelsToZero();
	            welcomeScreenRaised = true;
	            if (openInterfaceID != -1) {
	                RSInterface rsi = RSInterface.cache[openInterfaceID];
	                if (rsi.width == 512 && rsi.height == 334 && rsi.type == 0) {
	                    rsi.width = getClientWidth();
	                    rsi.height = getClientHeight();
	                }
	                drawInterface(rsi, 0, 8, 0);
	            }
	            RSInterface rsi = RSInterface.cache[fullscreenInterfaceID];
	            if (rsi.width == 512 && rsi.height == 334 && rsi.type == 0) {
                    rsi.width = getClientWidth();
                    rsi.height = getClientHeight();
	            }
	            int x = isFixed() ? 0 : (getClientWidth() / 2) - (765 / 2);
	            int y = isFixed() ? 8 : 8 + (getClientHeight() / 2) - (503 / 2);
                drawInterface(rsi, x, y, 0);
	            if (!menuOpen) {
	                processRightClick();
	                drawTooltip();
	            } else {
	                drawMenu();
	            }
	        }
	        drawCount++;
	        super.fullGameScreen.drawGraphics(0, 0, super.graphics);
	        return;
	    } else {
	        if (drawCount != 0) {
	            resetImageProducers2();
	        }
	    }
		if(welcomeScreenRaised) {
			welcomeScreenRaised = false;
			if (isFixed()) {
				backLeftIP1.drawGraphics(0, 4, super.graphics);
				backLeftIP2.drawGraphics(0, 357, super.graphics);
				backRightIP1.drawGraphics(722, 4, super.graphics);
				backRightIP2.drawGraphics(743, 205, super.graphics);
				backTopIP1.drawGraphics(0, 0, super.graphics);
				backVmidIP1.drawGraphics(516, 4, super.graphics);
				backVmidIP2.drawGraphics(516, 205, super.graphics);
				backVmidIP3.drawGraphics(496, 357, super.graphics);
				backVmidIP2_2.drawGraphics(0, 338, super.graphics);
			}
			redrawTabArea = true;
			inputTaken = true;
			tabAreaAltered = true;
			aBoolean1233 = true;
			if(loadingStage != 2) {
				gameArea.drawGraphics(getGameAreaX(), getGameAreaY(), super.graphics);
				if (isFixed()) {
					mapArea.drawGraphics(550, 4, super.graphics);
				}
			}
		}
		if(menuOpen && menuScreenArea == 1) {
			redrawTabArea = true;
		}
		if(invOverlayInterfaceID != -1) {
			boolean flag1 = method119(anInt945, invOverlayInterfaceID);
			if(flag1) {
				redrawTabArea = true;
			}
		}
		if(atInventoryInterfaceType == 2) {
			redrawTabArea = true;
		}
		if(activeInterfaceType == 2) {
			redrawTabArea = true;
		}
		if(redrawTabArea) {
			if (isFixed()) {
				drawTabArea();
			}
			redrawTabArea = false;
		}
		if(backDialogID == -1) {
			rsi.scrollPosition = anInt1211 - anInt1089 - 77;
			if(super.mouseX > 448 && super.mouseX < 560 && super.mouseY > 332)
				method65(463, 77, super.mouseX - 17, super.mouseY - 357, rsi, 0, false, anInt1211);
			int pos = anInt1211 - 77 - rsi.scrollPosition;
			if(pos < 0) {
				pos = 0;
			}
			if(pos > anInt1211 - 77) {
				pos = anInt1211 - 77;
			}
			if(anInt1089 != pos) {
				anInt1089 = pos;
				inputTaken = true;
			}
		}
		if(backDialogID != -1) {
			boolean flag2 = method119(anInt945, backDialogID);
			if(flag2) {
				inputTaken = true;
			}
		}
		if(atInventoryInterfaceType == 3) {
			inputTaken = true;
		}
		if(activeInterfaceType == 3) {
			inputTaken = true;
		}
		if(aString844 != null) {
			inputTaken = true;
		}
		if(menuOpen && menuScreenArea == 2) {
			inputTaken = true;
		}
		if(inputTaken) {
			if (isFixed()) {
				drawChatArea();
			}
			inputTaken = false;
		}
		if(loadingStage == 2) {
			method146();
		}
		if(loadingStage == 2) {
			if (isFixed()) {
				drawMinimap();
				mapArea.drawGraphics(550, 4, super.graphics);
			}
		}
		if(anInt1054 != -1) {
			tabAreaAltered = true;
		}
		if(tabAreaAltered && isFixed()) {
			if(anInt1054 != -1 && anInt1054 == tabID) {
				anInt1054 = -1;
				out.putOpCode(120);
				out.writeWordBigEndian(tabID);
			}
			tabAreaAltered = false;
			aRSImageProducer_1125.initDrawingArea();
			backHmid1.drawImage(0, 0);
			if(invOverlayInterfaceID == -1) {
				if(tabInterfaceIDs[tabID] != -1) {
					if(tabID == 0)
						redStone1.drawImage(22, 10);
					if(tabID == 1)
						redStone2.drawImage(54, 8);
					if(tabID == 2)
						redStone2.drawImage(82, 8);
					if(tabID == 3)
						redStone3.drawImage(110, 8);
					if(tabID == 4)
						redStone2_2.drawImage(153, 8);
					if(tabID == 5)
						redStone2_2.drawImage(181, 8);
					if(tabID == 6)
						redStone1_2.drawImage(209, 9);
				}
				if(tabInterfaceIDs[0] != -1 && (anInt1054 != 0 || currentTime % 20 < 10))
					sideIcons[0].drawImage(29, 13);
				if(tabInterfaceIDs[1] != -1 && (anInt1054 != 1 || currentTime % 20 < 10))
					sideIcons[1].drawImage(53, 11);
				if(tabInterfaceIDs[2] != -1 && (anInt1054 != 2 || currentTime % 20 < 10))
					sideIcons[2].drawImage(82, 11);
				if(tabInterfaceIDs[3] != -1 && (anInt1054 != 3 || currentTime % 20 < 10))
					sideIcons[3].drawImage(115, 12);
				if(tabInterfaceIDs[4] != -1 && (anInt1054 != 4 || currentTime % 20 < 10))
					sideIcons[4].drawImage(153, 13);
				if(tabInterfaceIDs[5] != -1 && (anInt1054 != 5 || currentTime % 20 < 10))
					sideIcons[5].drawImage(180, 11);
				if(tabInterfaceIDs[6] != -1 && (anInt1054 != 6 || currentTime % 20 < 10))
					sideIcons[6].drawImage(208, 13);
			}
			aRSImageProducer_1125.drawGraphics(516, 160, super.graphics);
			aRSImageProducer_1124.initDrawingArea();
			backBase2.drawImage(0, 0);
			if(invOverlayInterfaceID == -1) {
				if(tabInterfaceIDs[tabID] != -1) {
					if(tabID == 7)
						redStone1_3.drawImage(42, 0);
					if(tabID == 8)
						redStone2_3.drawImage(74, 0);
					if(tabID == 9)
						redStone2_3.drawImage(102, 0);
					if(tabID == 10)
						redStone3_2.drawImage(130, 1);
					if(tabID == 11)
						redStone2_4.drawImage(173, 0);
					if(tabID == 12)
						redStone2_4.drawImage(201, 0);
					if(tabID == 13)
						redStone1_4.drawImage(229, 0);
				}
				if(tabInterfaceIDs[8] != -1 && (anInt1054 != 8 || currentTime % 20 < 10))
					sideIcons[7].drawImage(74, 2);
				if(tabInterfaceIDs[9] != -1 && (anInt1054 != 9 || currentTime % 20 < 10))
					sideIcons[8].drawImage(102, 3);
				if(tabInterfaceIDs[10] != -1 && (anInt1054 != 10 || currentTime % 20 < 10))
					sideIcons[9].drawImage(137, 4);
				if(tabInterfaceIDs[11] != -1 && (anInt1054 != 11 || currentTime % 20 < 10))
					sideIcons[10].drawImage(174, 2);
				if(tabInterfaceIDs[12] != -1 && (anInt1054 != 12 || currentTime % 20 < 10))
					sideIcons[11].drawImage(201, 2);
				if(tabInterfaceIDs[13] != -1 && (anInt1054 != 13 || currentTime % 20 < 10))
					sideIcons[12].drawImage(226, 2);
			}
			aRSImageProducer_1124.drawGraphics(496, 466, super.graphics);
			gameArea.initDrawingArea();
		}
		if(aBoolean1233 && isFixed()) {
			aBoolean1233 = false;
			aRSImageProducer_1123.initDrawingArea();
			backBase1.drawImage(0, 0);
			regular.drawCenteredString("Public chat", 55, 28, 0xffffff, true);
			if(publicChatMode == 0)
				regular.drawCenteredString("On", 55, 41, 65280, true);
			if(publicChatMode == 1)
				regular.drawCenteredString("Friends", 55, 41, 0xffff00, true);
			if(publicChatMode == 2)
				regular.drawCenteredString("Off", 55, 41, 0xff0000, true);
			if(publicChatMode == 3)
				regular.drawCenteredString("Hide", 55, 41, 65535, true);
			regular.drawCenteredString("Private chat", 184, 28, 0xffffff, true);
			if(privateChatMode == 0)
				regular.drawCenteredString("On", 184, 41, 65280, true);
			if(privateChatMode == 1)
				regular.drawCenteredString("Friends", 184, 41, 0xffff00, true);
			if(privateChatMode == 2)
				regular.drawCenteredString("Off", 184, 41, 0xff0000, true);
			regular.drawCenteredString("Trade/compete", 324, 28, 0xffffff, true);
			if(tradeMode == 0)
				regular.drawCenteredString("On", 324, 41, 65280, true);
			if(tradeMode == 1)
				regular.drawCenteredString("Friends", 324, 41, 0xffff00, true);
			if(tradeMode == 2)
				regular.drawCenteredString("Off", 324, 41, 0xff0000, true);
			regular.drawCenteredString("Report abuse", 458, 33, 0xffffff, true);
			aRSImageProducer_1123.drawGraphics(0, 453, super.graphics);
			gameArea.initDrawingArea();
		}
		anInt945 = 0;
	}

	private boolean buildFriendsListMenu(RSInterface class9)
	{
		int i = class9.contentType;
		if(i >= 1 && i <= 200 || i >= 701 && i <= 900)
		{
			if(i >= 801)
				i -= 701;
			else
				if(i >= 701)
					i -= 601;
				else
					if(i >= 101)
						i -= 101;
					else
						i--;
			menuActionName[menuActionRow] = "Remove @whi@" + friendsList[i];
			menuActionID[menuActionRow] = 792;
			menuActionRow++;
			menuActionName[menuActionRow] = "Message @whi@" + friendsList[i];
			menuActionID[menuActionRow] = 639;
			menuActionRow++;
			return true;
		}
		if(i >= 401 && i <= 500)
		{
			menuActionName[menuActionRow] = "Remove @whi@" + class9.disabledText;
			menuActionID[menuActionRow] = 322;
			menuActionRow++;
			return true;
		} else
		{
			return false;
		}
	}

	private void method104()
	{
		StillGraphics class30_sub2_sub4_sub3 = (StillGraphics)aClass19_1056.head();
		for(; class30_sub2_sub4_sub3 != null; class30_sub2_sub4_sub3 = (StillGraphics)aClass19_1056.next())
			if(class30_sub2_sub4_sub3.anInt1560 != floor_level || class30_sub2_sub4_sub3.aBoolean1567)
				class30_sub2_sub4_sub3.remove();
			else
				if(currentTime >= class30_sub2_sub4_sub3.anInt1564)
				{
					class30_sub2_sub4_sub3.method454(anInt945);
					if(class30_sub2_sub4_sub3.aBoolean1567)
						class30_sub2_sub4_sub3.remove();
					else
						worldController.method285(class30_sub2_sub4_sub3.anInt1560, 0, class30_sub2_sub4_sub3.anInt1563, -1, class30_sub2_sub4_sub3.anInt1562, 60, class30_sub2_sub4_sub3.anInt1561, class30_sub2_sub4_sub3, false);
				}

	}

	private void drawInterface(RSInterface rsi, int pos_x, int pos_y, int offsetY) {
		if(rsi.type != 0 || rsi.children == null) {
			return;
		}
		if(rsi.showInterface && anInt1026 != rsi.id && anInt1048 != rsi.id && anInt1039 != rsi.id) {
			return;
		}
		int startX = RSDrawingArea.startX;
		int startY = RSDrawingArea.startY;
		int endX = RSDrawingArea.endX;
		int endY = RSDrawingArea.endY;
		RSDrawingArea.setBounds(pos_x, pos_x + rsi.width, pos_y, pos_y + rsi.height);
		int children = rsi.children.length;
		for(int index = 0; index < children; index++) {
			int x = rsi.childX[index] + pos_x;
			int y = (rsi.childY[index] + pos_y) - offsetY;
			RSInterface child = RSInterface.cache[rsi.children[index]];
			x += child.drawOffsetX;
			y += child.drawOffsetY;
			if(child.contentType > 0)
				drawFriendsListOrWelcomeScreen(child);
			if(child.type == 0) {
				if(child.scrollPosition > child.scrollMax - child.height)
					child.scrollPosition = child.scrollMax - child.height;
				if(child.scrollPosition < 0) {
					child.scrollPosition = 0;
				}
				drawInterface(child, x, y, child.scrollPosition);
				if(child.scrollMax > child.height) {
					drawScrollbar(child.height, child.scrollPosition, y, x + child.width, child.scrollMax);
				}
			} else {
				if(child.type != 1)
					if(child.type == 2) {
						int i3 = 0;
						for(int l3 = 0; l3 < child.height; l3++) {
							for(int l4 = 0; l4 < child.width; l4++) {
								int item_x = x + l4 * (32 + child.invSpritePadX);
								int item_y = y + l3 * (32 + child.invSpritePadY);
								if(i3 < 20) {
									item_x += child.spritesX[i3];
									item_y += child.spritesY[i3];
								}
								if(child.inventory[i3] > 0) {
									int offset_x = 0;
									int offset_y = 0;
									int j9 = child.inventory[i3] - 1;
									if(item_x > RSDrawingArea.startX - 32 && item_x < RSDrawingArea.endX && item_y > RSDrawingArea.startY - 32 && item_y < RSDrawingArea.endY || activeInterfaceType != 0 && anInt1085 == i3) {
										int l9 = 0;
										if(itemSelected == 1 && anInt1283 == i3 && anInt1284 == child.id)
											l9 = 0xffffff;
										RSImage image = ItemDef.getSprite(j9, child.inventoryAmount[i3], l9);
										if(image != null) {
											if(activeInterfaceType != 0 && anInt1085 == i3 && anInt1084 == child.id) {
												offset_x = super.mouseX - anInt1087;
												offset_y = super.mouseY - anInt1088;
												if(offset_x < 5 && offset_x > -5)
													offset_x = 0;
												if(offset_y < 5 && offset_y > -5)
													offset_y = 0;
												if(anInt989 < 5) {
													offset_x = 0;
													offset_y = 0;
												}
												image.drawImage(item_x + offset_x, item_y + offset_y, 128);
												if(item_y + offset_y < RSDrawingArea.startY && rsi.scrollPosition > 0) {
													int i10 = (anInt945 * (RSDrawingArea.startY - item_y - offset_y)) / 3;
													if(i10 > anInt945 * 10)
														i10 = anInt945 * 10;
													if(i10 > rsi.scrollPosition)
														i10 = rsi.scrollPosition;
													rsi.scrollPosition -= i10;
													anInt1088 += i10;
												}
												if(item_y + offset_y + 32 > RSDrawingArea.endY && rsi.scrollPosition < rsi.scrollMax - rsi.height) {
													int j10 = (anInt945 * ((item_y + offset_y + 32) - RSDrawingArea.endY)) / 3;
													if(j10 > anInt945 * 10)
														j10 = anInt945 * 10;
													if(j10 > rsi.scrollMax - rsi.height - rsi.scrollPosition)
														j10 = rsi.scrollMax - rsi.height - rsi.scrollPosition;
													rsi.scrollPosition += j10;
													anInt1088 -= j10;
												}
											} else
												if(atInventoryInterfaceType != 0 && atInventoryIndex == i3 && atInventoryInterface == child.id)
													image.drawImage(item_x, item_y, 128);
												else
													image.drawImage(item_x, item_y);
											if(image.maxWidth == 33 || child.inventoryAmount[i3] != 1) {
												int k10 = child.inventoryAmount[i3];
												small.drawBasicString(intToKOrMil(k10), item_x + 1 + offset_x, item_y + 10 + offset_y, 0);
												small.drawBasicString(intToKOrMil(k10), item_x + offset_x, item_y + 9 + offset_y, 0xffff00);
											}
										}
									}
								} else
									if(child.sprites != null && i3 < 20) {
										RSImage image = child.sprites[i3];
										if(image != null) {
											image.drawImage(item_x, item_y);
										}
									}
								i3++;
							}
						}
					} else if(child.type == 3) {
						boolean hovered = false;
						if(anInt1039 == child.id || anInt1048 == child.id || anInt1026 == child.id) {
							hovered = true;
						}
						int color;
						if(interfaceIsSelected(child)) {
							color = child.enabledColor;
							if(hovered && child.enabledHoverColor != 0) {
								color = child.enabledHoverColor;
							}
						} else {
							color = child.disabledColor;
							if(hovered && child.disabledHoverColor != 0) {
								color = child.disabledHoverColor;
							}
						}
						if(child.alpha == 0) {
							if(child.filled) {
								RSDrawingArea.drawFilledPixels(x, y, child.width, child.height, color);
							} else {
								RSDrawingArea.drawUnfilledPixels(x, y, child.width, child.height, color);
							}
						} else {
							if(child.filled) {
								RSDrawingArea.method335(color, y, child.width, child.height, 256 - (child.alpha & 0xff), x);
							} else {
								RSDrawingArea.method338(y, child.height, 256 - (child.alpha & 0xff), color, child.width, x);
							}
						}
					} else if(child.type == 4) {
						RSFont font = child.font;
						String text = child.disabledText;
						boolean hovered = false;
						if(anInt1039 == child.id || anInt1048 == child.id || anInt1026 == child.id) {
							hovered = true;
						}
						int color;
						if(interfaceIsSelected(child)) {
							color = child.enabledColor;
							if(hovered && child.enabledHoverColor != 0) {
								color = child.enabledHoverColor;
							}
							if(child.enabledText.length() > 0) {
								text = child.enabledText;
							}
						} else {
							color = child.disabledColor;
							if(hovered && child.disabledHoverColor != 0) {
								color = child.disabledHoverColor;
							}
						}
						if(child.actionType == 6 && aBoolean1149) {
							text = "Please wait...";
							color = child.disabledColor;
						}
						if(RSDrawingArea.width == 479) {
							if(color == 0xffff00) {
								color = 255;
							}
							if(color == 49152) {
								color = 0xffffff;
							}
						}
						for(int textY = y + font.baseHeight; text.length() > 0; textY += font.baseHeight) {
							if(text.indexOf("%") != -1) {
								do {
									int valueIndex = text.indexOf("%1");
									if(valueIndex == -1)
										break;
									text = text.substring(0, valueIndex) + interfaceIntToString(extractValue(child, 0)) + text.substring(valueIndex + 2);
								} while(true);
								do {
									int valueIndex = text.indexOf("%2");
									if(valueIndex == -1)
										break;
									text = text.substring(0, valueIndex) + interfaceIntToString(extractValue(child, 1)) + text.substring(valueIndex + 2);
								} while(true);
								do {
									int valueIndex = text.indexOf("%3");
									if(valueIndex == -1)
										break;
									text = text.substring(0, valueIndex) + interfaceIntToString(extractValue(child, 2)) + text.substring(valueIndex + 2);
								} while(true);
								do {
									int valueIndex = text.indexOf("%4");
									if(valueIndex == -1)
										break;
									text = text.substring(0, valueIndex) + interfaceIntToString(extractValue(child, 3)) + text.substring(valueIndex + 2);
								} while(true);
								do {
									int valueIndex = text.indexOf("%5");
									if(valueIndex == -1)
										break;
									text = text.substring(0, valueIndex) + interfaceIntToString(extractValue(child, 4)) + text.substring(valueIndex + 2);
								} while(true);
							}
							int newLineIndex = text.indexOf("\\n");
							String finalText;
							if(newLineIndex != -1) {
								finalText = text.substring(0, newLineIndex);
								text = text.substring(newLineIndex + 2);
							} else {
								finalText = text;
								text = "";
							}
							if(child.centered) {
								font.drawCenteredString(finalText, x + child.width / 2, textY, color, child.shadowed);
							} else {
								font.drawShadowedString(finalText, x, textY, color, child.shadowed);
							}
						}
				} else if(child.type == 5) {
					RSImage sprite;
					if(interfaceIsSelected(child)) {
						sprite = child.enabledSprite;
					} else {
						sprite = child.disabledSprite;
					}
					if(sprite != null) {
						sprite.drawImage(x, y);
					}
				} else if(child.type == 6) {
					int k3 = Rasterizer.centerX;
					int j4 = Rasterizer.centerY;
					Rasterizer.centerX = x + child.width / 2;
					Rasterizer.centerY = y + child.height / 2;
					int i5 = Rasterizer.SINE[child.rotationX] * child.zoom >> 16;
					int l5 = Rasterizer.COSINE[child.rotationX] * child.zoom >> 16;
					boolean enabled = interfaceIsSelected(child);
					int anim;
					if(enabled) {
						anim = child.enabledAnimation;
					} else {
						anim = child.disabledAnimation;
					}
					Model model;
					if(anim == -1) {
						model = child.getAnimatedModel(-1, -1, enabled);
					} else {
						Sequence animation = Sequence.getSeq(anim);
						model = child.getAnimatedModel(animation.anIntArray354[child.currentFrame], animation.frames[child.currentFrame], enabled);
					}
					if(model != null) {
						model.method482(child.rotationY, 0, child.rotationX, 0, i5, l5);
					}
					Rasterizer.centerX = k3;
					Rasterizer.centerY = j4;
				} else if(child.type == 7) {
					RSFont font = child.font;
					int k4 = 0;
					for(int j5 = 0; j5 < child.height; j5++) {
						for(int i6 = 0; i6 < child.width; i6++) {
							if(child.inventory[k4] > 0) {
								ItemDef itemDef = ItemDef.getDef(child.inventory[k4] - 1);
								String s2 = itemDef.name;
								if(itemDef.stackable || child.inventoryAmount[k4] != 1)
									s2 = s2 + " x" + formatAmount(child.inventoryAmount[k4]);
								int i9 = x + i6 * (115 + child.invSpritePadX);
								int k9 = y + j5 * (12 + child.invSpritePadY);
								if(child.centered)
									font.drawCenteredString(s2, i9 + child.width / 2, k9, child.disabledColor, child.shadowed);
								else
									font.drawShadowedString(s2, i9, k9, child.disabledColor, child.shadowed);
							}
							k4++;
						}

					}
				} else if (child.type == 8 && (anInt1500 == child.id || anInt1044 == child.id || anInt1129 == child.id) && anInt1501 == 50) {
                    int boxWidth = 0;
					int boxHeight = 0;
					RSFont font = regular;
					for (String s1 = child.disabledText; s1.length() > 0;) {
						if (s1.indexOf("%") != -1) {
							do {
								int k7 = s1.indexOf("%1");
								if (k7 == -1)
									break;
								s1 = s1.substring(0, k7) + interfaceIntToString(extractValue(child, 0)) + s1.substring(k7 + 2);
							} while (true);
								do {
									int l7 = s1.indexOf("%2");
									if (l7 == -1)
										break;
									s1 = s1.substring(0, l7) + interfaceIntToString(extractValue(child, 1)) + s1.substring(l7 + 2);
								} while (true);
								do {
									int i8 = s1.indexOf("%3");
									if (i8 == -1)
										break;
									s1 = s1.substring(0, i8) + interfaceIntToString(extractValue(child, 2)) + s1.substring(i8 + 2);
								} while (true);
								do {
									int j8 = s1.indexOf("%4");
									if (j8 == -1)
										break;
									s1 = s1.substring(0, j8) + interfaceIntToString(extractValue(child, 3)) + s1.substring(j8 + 2);
								} while (true);
								do {
									int k8 = s1.indexOf("%5");
									if (k8 == -1)
										break;
									s1 = s1.substring(0, k8) + interfaceIntToString(extractValue(child, 4)) + s1.substring(k8 + 2);
								} while (true);
							}
							int l7 = s1.indexOf("\\n");
							String s4;
							if (l7 != -1) {
								s4 = s1.substring(0, l7);
								s1 = s1.substring(l7 + 2);
							} else {
								s4 = s1;
								s1 = "";
							}
							int j10 = font.getTextWidth(s4);
							if (j10 > boxWidth) {
								boxWidth = j10;
							}
							boxHeight += font.baseHeight + 1;
						}
						boxWidth += 6;
						boxHeight += 7;
						int xPos = (x + child.width) - 5 - boxWidth;
						int yPos = y + child.height + 5;
						if (xPos < x + 5)
							xPos = x + 5;
						if (xPos + boxWidth > pos_x + rsi.width)
							xPos = (pos_x + rsi.width) - boxWidth;
						if (yPos + boxHeight > offsetY + rsi.height)
							yPos = (y - boxHeight);
						RSDrawingArea.drawFilledPixels(xPos, yPos, boxWidth, boxHeight, 0xFFFFA0);
						RSDrawingArea.drawUnfilledPixels(xPos, yPos, boxWidth, boxHeight, 0);
						String s2 = child.disabledText;
						for (int j11 = yPos + font.baseHeight + 2; s2.length() > 0; j11 += font.baseHeight + 1) {
							if (s2.indexOf("%") != -1) {
								do {
									int k7 = s2.indexOf("%1");
									if (k7 == -1)
										break;
									s2 = s2.substring(0, k7) + interfaceIntToString(extractValue(child, 0)) + s2.substring(k7 + 2);
								} while (true);
								do {
									int l7 = s2.indexOf("%2");
									if (l7 == -1)
										break;
									s2 = s2.substring(0, l7) + interfaceIntToString(extractValue(child, 1)) + s2.substring(l7 + 2);
								} while (true);
								do {
									int i8 = s2.indexOf("%3");
									if (i8 == -1)
										break;
									s2 = s2.substring(0, i8) + interfaceIntToString(extractValue(child, 2)) + s2.substring(i8 + 2);
								} while (true);
								do {
									int j8 = s2.indexOf("%4");
									if (j8 == -1)
										break;
									s2 = s2.substring(0, j8) + interfaceIntToString(extractValue(child, 3)) + s2.substring(j8 + 2);
								} while (true);
								do {
									int k8 = s2.indexOf("%5");
									if (k8 == -1)
										break;
									s2 = s2.substring(0, k8) + interfaceIntToString(extractValue(child, 4)) + s2.substring(k8 + 2);
								} while (true);
							}
							int l11 = s2.indexOf("\\n");
							String s5;
							if (l11 != -1) {
								s5 = s2.substring(0, l11);
								s2 = s2.substring(l11 + 2);
							} else {
								s5 = s2;
								s2 = "";
							}
							if (child.centered) {
								font.drawCenteredString(s5, xPos + child.width / 2, yPos, 0, false);
							} else {
								if (s5.contains("\\r")) {
									String text = s5.substring(0, s5.indexOf("\\r"));
									String text2 = s5.substring(s5.indexOf("\\r") + 2);
									font.drawBasicString(text, xPos + 3, j11, 0);
									int rightX = boxWidth + xPos - font.getTextWidth(text2) - 2;
									font.drawBasicString(text2, rightX, j11, 0);
									System.out.println("Box: " + boxWidth + "");
								} else
									font.drawBasicString(s5, xPos + 3, j11, 0);
							}
						}
				}
			}
		}
		RSDrawingArea.setBounds(startX, endX, startY, endY);
	}

	private void randomizeBackground(IndexedImage background)
	{
		int j = 256;
		for(int k = 0; k < anIntArray1190.length; k++)
			anIntArray1190[k] = 0;

		for(int l = 0; l < 5000; l++)
		{
			int i1 = (int)(Math.random() * 128D * (double)j);
			anIntArray1190[i1] = (int)(Math.random() * 256D);
		}

		for(int j1 = 0; j1 < 20; j1++)
		{
			for(int k1 = 1; k1 < j - 1; k1++)
			{
				for(int i2 = 1; i2 < 127; i2++)
				{
					int k2 = i2 + (k1 << 7);
					anIntArray1191[k2] = (anIntArray1190[k2 - 1] + anIntArray1190[k2 + 1] + anIntArray1190[k2 - 128] + anIntArray1190[k2 + 128]) / 4;
				}

			}

			int ai[] = anIntArray1190;
			anIntArray1190 = anIntArray1191;
			anIntArray1191 = ai;
		}

		if(background != null)
		{
			int l1 = 0;
			for(int j2 = 0; j2 < background.myHeight; j2++)
			{
				for(int l2 = 0; l2 < background.myWidth; l2++)
					if(background.myPixels[l1++] != 0)
					{
						int i3 = l2 + 16 + background.anInt1454;
						int j3 = j2 + 16 + background.anInt1455;
						int k3 = i3 + (j3 << 7);
						anIntArray1190[k3] = 0;
					}

			}

		}
	}

	private void method107(int i, int j, ByteBuffer stream, Player player)
	{
		if((i & 0x400) != 0)
		{
			player.anInt1543 = stream.method428();
			player.anInt1545 = stream.method428();
			player.anInt1544 = stream.method428();
			player.anInt1546 = stream.method428();
			player.anInt1547 = stream.method436() + currentTime;
			player.anInt1548 = stream.method435() + currentTime;
			player.turnInfo = stream.method428();
			player.method446();
		}
		if((i & 0x100) != 0)
		{
			player.graphicsId = stream.method434();
			int k = stream.getInt();
			player.graphicsHeight = k >> 16;
						player.graphicsDelay = currentTime + (k & 0xffff);
						player.anInt1521 = 0;
						player.anInt1522 = 0;
						if(player.graphicsDelay > currentTime)
							player.anInt1521 = -1;
						if(player.graphicsId == 65535)
							player.graphicsId = -1;
		}
		if((i & 8) != 0)
		{
			int l = stream.method434();
			if(l == 65535)
				l = -1;
			int i2 = stream.method427();
			if(l == player.anim && l != -1)
			{
				int i3 = Sequence.getSeq(l).anInt365;
				if(i3 == 1)
				{
					player.anInt1527 = 0;
					player.anInt1528 = 0;
					player.anInt1529 = i2;
					player.anInt1530 = 0;
				}
				if(i3 == 2)
					player.anInt1530 = 0;
			} else
				if(l == -1 || player.anim == -1 || Sequence.getSeq(l).anInt359 >= Sequence.getSeq(player.anim).anInt359)
				{
					player.anim = l;
					player.anInt1527 = 0;
					player.anInt1528 = 0;
					player.anInt1529 = i2;
					player.anInt1530 = 0;
					player.anInt1542 = player.pathLength;
				}
		}
		if((i & 4) != 0)
		{
			player.textSpoken = stream.getString();
			if(player.textSpoken.charAt(0) == '~')
			{
				player.textSpoken = player.textSpoken.substring(1);
				pushMessage(player.name, player.textSpoken, 2);
			} else
				if(player == myPlayer)
					pushMessage(player.name, player.textSpoken, 2);
			player.anInt1513 = 0;
			player.anInt1531 = 0;
			player.textCycle = 150;
		}
		if((i & 0x80) != 0)
		{
			int i1 = stream.method434();
			int rights = stream.getUByte();
			int text = stream.method427();
			int k3 = stream.offset;
			if(player.name != null && player.visible) {
				long name = TextUtils.longForName(player.name);
				boolean ignored = false;
				if(rights <= 1) {
					for(int index = 0; index < ignoreCount; index++) {
						if(ignoreListAsLongs[index] != name) {
							continue;
						}
						ignored = true;
						break;
					}
				}
				if(!ignored && anInt1251 == 0) {
					try {
						aStream_834.offset = 0;
						stream.method442(text, 0, aStream_834.buffer);
						aStream_834.offset = 0;
						String spoken = TextInput.method525(text, aStream_834);
						spoken = Censor.censor(spoken);
						player.textSpoken = spoken;
						player.anInt1513 = i1 >> 8;
						player.rights = rights;
						player.anInt1531 = i1 & 0xff;
						player.textCycle = 150;
						if (rights != 0) {
							pushMessage(getPrefix(rights) + player.name, spoken, 1);
						} else {
							pushMessage(player.name, spoken, 2);
						}
					} catch(Exception exception) {
						signlink.reportError("cde2");
					}
				}
			}
			stream.offset = k3 + text;
		}
		if((i & 1) != 0)
		{
			player.interactingEntity = stream.method434();
			if(player.interactingEntity == 65535)
				player.interactingEntity = -1;
		}
		if((i & 0x10) != 0)
		{
			int j1 = stream.method427();
			byte abyte0[] = new byte[j1];
			ByteBuffer stream_1 = new ByteBuffer(abyte0);
			stream.getBytes(j1, 0, abyte0);
			aStreamArray895s[j] = stream_1;
			player.updatePlayer(stream_1);
		}
		if((i & 2) != 0)
		{
			player.faceX = stream.method436();
			player.faceY = stream.method434();
		}
		if((i & 0x20) != 0)
		{
			int k1 = stream.getUByte();
			int k2 = stream.method426();
			player.updateHitData(k2, k1, currentTime);
			player.loopCycleStatus = currentTime + 300;
			player.currentHealth = stream.method427();
			player.maxHealth = stream.getUByte();
		}
		if((i & 0x200) != 0)
		{
			int l1 = stream.getUByte();
			int l2 = stream.method428();
			player.updateHitData(l2, l1, currentTime);
			player.loopCycleStatus = currentTime + 300;
			player.currentHealth = stream.getUByte();
			player.maxHealth = stream.method427();
		}
	}

	private void method108()
	{
		try
		{
			int j = myPlayer.currentX + anInt1278;
			int k = myPlayer.currentY + anInt1131;
			if(anInt1014 - j < -500 || anInt1014 - j > 500 || anInt1015 - k < -500 || anInt1015 - k > 500)
			{
				anInt1014 = j;
				anInt1015 = k;
			}
			if(anInt1014 != j)
				anInt1014 += (j - anInt1014) / 16;
			if(anInt1015 != k)
				anInt1015 += (k - anInt1015) / 16;
			if(super.keyArray[1] == 1)
				anInt1186 += (-24 - anInt1186) / 2;
			else
				if(super.keyArray[2] == 1)
					anInt1186 += (24 - anInt1186) / 2;
				else
					anInt1186 /= 2;
			if(super.keyArray[3] == 1)
				anInt1187 += (12 - anInt1187) / 2;
			else
				if(super.keyArray[4] == 1)
					anInt1187 += (-12 - anInt1187) / 2;
				else
					anInt1187 /= 2;
			minimapInt1 = minimapInt1 + anInt1186 / 2 & 0x7ff;
			anInt1184 += anInt1187 / 2;
			if(anInt1184 < 128)
				anInt1184 = 128;
			if(anInt1184 > 383)
				anInt1184 = 383;
			int l = anInt1014 >> 7;
					int i1 = anInt1015 >> 7;
				int j1 = method42(floor_level, anInt1015, anInt1014);
				int k1 = 0;
				if(l > 3 && i1 > 3 && l < 100 && i1 < 100)
				{
					for(int l1 = l - 4; l1 <= l + 4; l1++)
					{
						for(int k2 = i1 - 4; k2 <= i1 + 4; k2++)
						{
							int l2 = floor_level;
							if(l2 < 3 && (byteGroundArray[1][l1][k2] & 2) == 2)
								l2++;
							int i3 = j1 - intGroundArray[l2][l1][k2];
							if(i3 > k1)
								k1 = i3;
						}

					}

				}
				anInt1005++;
				if(anInt1005 > 1512)
				{
					anInt1005 = 0;
					out.putOpCode(77);
					out.writeWordBigEndian(0);
					int i2 = out.offset;
					out.writeWordBigEndian((int)(Math.random() * 256D));
					out.writeWordBigEndian(101);
					out.writeWordBigEndian(233);
					out.putShort(45092);
					if((int)(Math.random() * 2D) == 0)
						out.putShort(35784);
					out.writeWordBigEndian((int)(Math.random() * 256D));
					out.writeWordBigEndian(64);
					out.writeWordBigEndian(38);
					out.putShort((int)(Math.random() * 65536D));
					out.putShort((int)(Math.random() * 65536D));
					out.putBytes(out.offset - i2);
				}
				int j2 = k1 * 192;
				if(j2 > 0x17f00)
					j2 = 0x17f00;
				if(j2 < 32768)
					j2 = 32768;
				if(j2 > anInt984)
				{
					anInt984 += (j2 - anInt984) / 24;
					return;
				}
				if(j2 < anInt984)
				{
					anInt984 += (j2 - anInt984) / 80;
				}
		}
		catch(Exception _ex)
		{
			signlink.reportError("glfc_ex " + myPlayer.currentX + "," + myPlayer.currentY + "," + anInt1014 + "," + anInt1015 + "," + anInt1069 + "," + anInt1070 + "," + baseX + "," + baseY);
			throw new RuntimeException("eek");
		}
	}

	public void processDrawing()
	{
		if(rsAlreadyLoaded || loadingError || genericLoadingError)
		{
			showErrorScreen();
			return;
		}
		anInt1061++;
		if(!loggedIn)
			displayTitleScreen(false);
		else
			drawGameScreen();
		anInt1213 = 0;
	}

	private boolean isFriendOrSelf(String s)
	{
		if(s == null)
			return false;
		for(int i = 0; i < friendsCount; i++)
			if(s.equalsIgnoreCase(friendsList[i]))
				return true;
		return s.equalsIgnoreCase(myPlayer.name);
	}

	private static String combatDiffColor(int i, int j)
	{
		int k = i - j;
		if(k < -9)
			return "@red@";
		if(k < -6)
			return "@or3@";
		if(k < -3)
			return "@or2@";
		if(k < 0)
			return "@or1@";
		if(k > 9)
			return "@gre@";
		if(k > 6)
			return "@gr3@";
		if(k > 3)
			return "@gr2@";
		if(k > 0)
			return "@gr1@";
		else
			return "@yel@";
	}

	private void setWaveVolume(int i)
	{
		signlink.wavevol = i;
	}

	private void draw3dScreen() {
		drawSplitPrivateChat();
		if(crossType == 1) {
			crosses[crossIndex / 100].drawImage(crossX - 8 - 4, crossY - 8 - 4);
			anInt1142++;
			if(anInt1142 > 67) {
				anInt1142 = 0;
				out.putOpCode(78);
			}
		}
		if(crossType == 2)
			crosses[4 + crossIndex / 100].drawImage(crossX - 8 - 4, crossY - 8 - 4);
		if(anInt1018 != -1) {
			method119(anInt945, anInt1018);
			drawInterface(RSInterface.cache[anInt1018], 0, 0, 0);
		}
		if(openInterfaceID != -1) {
			method119(anInt945, openInterfaceID);
			RSInterface rsi = RSInterface.cache[openInterfaceID];
			int x = isFixed() ? 0 : (getClientWidth() / 2) - (rsi.width / 2);
			int y = isFixed() ? 0 : (getClientHeight() / 2) - (rsi.height / 2);
			drawInterface(RSInterface.cache[openInterfaceID], x, y, 0);
		}
		method70();
		if(!menuOpen)
		{
			processRightClick();
			drawTooltip();
		} else
			if(menuScreenArea == 0)
				drawMenu();
		if(anInt1055 == 1)
			headIcons[1].drawImage(472, 296);
		if(fpsOn)
		{
			char c = '\u01FB';
			int k = 20;
			int i1 = 0xffff00;
			if(super.fps < 15)
				i1 = 0xff0000;
			regular.drawTextRA("Fps:" + super.fps, c, i1, k);
			k += 15;
			Runtime runtime = Runtime.getRuntime();
			int j1 = (int)((runtime.totalMemory() - runtime.freeMemory()) / 1024L);
			i1 = 0xffff00;
			if(j1 > 0x2000000 && lowMem)
				i1 = 0xff0000;
			regular.drawTextRA("Mem:" + j1 + "k", c, 0xffff00, k);
			k += 15;
		}
		if(anInt1104 != 0)
		{
			int j = anInt1104 / 50;
			int l = j / 60;
			j %= 60;
			if(j < 10)
				regular.drawBasicString("System update in: " + l + ":0" + j, 4, 329, 0xffff00);
			else
				regular.drawBasicString("System update in: " + l + ":" + j, 4, 329, 0xffff00);
			anInt849++;
			if(anInt849 > 75)
			{
				anInt849 = 0;
				out.putOpCode(148);
			}
		}
	}

	private void addIgnore(long l)
	{
		try
		{
			if(l == 0L)
				return;
			if(ignoreCount >= 100)
			{
				pushMessage("", "Your ignore list is full. Max of 100 hit", 0);
				return;
			}
			String s = TextUtils.fixName(TextUtils.nameForLong(l));
			for(int j = 0; j < ignoreCount; j++)
				if(ignoreListAsLongs[j] == l)
				{
					pushMessage("", s + " is already on your ignore list", 0);
					return;
				}
			for(int k = 0; k < friendsCount; k++)
				if(friendsListAsLongs[k] == l)
				{
					pushMessage("", "Please remove " + s + " from your friend list first", 0);
					return;
				}

			ignoreListAsLongs[ignoreCount++] = l;
			redrawTabArea = true;
			out.putOpCode(133);
			out.putLong(l);
			return;
		}
		catch(RuntimeException runtimeexception)
		{
			signlink.reportError("45688, " + l + ", " + 4 + ", " + runtimeexception.toString());
		}
		throw new RuntimeException();
	}

	private void method114()
	{
		for(int i = -1; i < playerCount; i++)
		{
			int j;
			if(i == -1)
				j = myPlayerIndex;
			else
				j = playerIndices[i];
			Player player = playerArray[j];
			if(player != null)
				method96(player);
		}

	}

	private void method115()
	{
		if(loadingStage == 2)
		{
			for(GameObjectSpawnRequest class30_sub1 = (GameObjectSpawnRequest)deque.head(); class30_sub1 != null; class30_sub1 = (GameObjectSpawnRequest)deque.next())
			{
				if(class30_sub1.anInt1294 > 0)
					class30_sub1.anInt1294--;
				if(class30_sub1.anInt1294 == 0)
				{
					if(class30_sub1.id < 0 || MapRegion.method178(class30_sub1.id, class30_sub1.type))
					{
						method142(class30_sub1.y, class30_sub1.plane, class30_sub1.face, class30_sub1.type, class30_sub1.x, class30_sub1.anInt1296, class30_sub1.id);
						class30_sub1.remove();
					}
				} else
				{
					if(class30_sub1.anInt1302 > 0)
						class30_sub1.anInt1302--;
					if(class30_sub1.anInt1302 == 0 && class30_sub1.x >= 1 && class30_sub1.y >= 1 && class30_sub1.x <= 102 && class30_sub1.y <= 102 && (class30_sub1.id2 < 0 || MapRegion.method178(class30_sub1.id2, class30_sub1.type2)))
					{
						method142(class30_sub1.y, class30_sub1.plane, class30_sub1.face2, class30_sub1.type2, class30_sub1.x, class30_sub1.anInt1296, class30_sub1.id2);
						class30_sub1.anInt1302 = -1;
						if(class30_sub1.id2 == class30_sub1.id && class30_sub1.id == -1)
							class30_sub1.remove();
						else
							if(class30_sub1.id2 == class30_sub1.id && class30_sub1.face2 == class30_sub1.face && class30_sub1.type2 == class30_sub1.type)
								class30_sub1.remove();
					}
				}
			}

		}
	}

	private void determineMenuSize() {
		int width = bold.getEffectTextWidth("Choose Option");
		for(int j = 0; j < menuActionRow; j++) {
			int item_width = bold.getEffectTextWidth(menuActionName[j]);
			if(item_width > width) {
				width = item_width;
			}
		}
		width += 8;
		int height = 15 * menuActionRow + 21;
		if (!isFixed()) {
			int startX = isFixed() ? 4 : 0;
			int endX = isFixed() ? 516 : getClientWidth();
			int startY = isFixed() ? 4 : 0;
			int endY = isFixed() ? 338 : getClientHeight();
			if(super.saveClickX > startX && super.saveClickY > startY && super.saveClickX < endX && super.saveClickY < endY) {
				int x = super.saveClickX - startX - width / 2;
				if(x + width > (endX - startX)) {
					x = (endX - startX) - width;
				}
				if(x < 0) {
					x = 0;
				}
				int y = super.saveClickY - startY;
				if(y + height > (endY - startY)) {
					y = (endY - startY) - height;
				}
				if(y < 0) {
					y = 0;
				}
				menuOpen = true;
				menuScreenArea = 0;
				menuOffsetX = x;
				menuOffsetY = y;
				menuWidth = width;
				menuHeight = 15 * menuActionRow + 22;
			}
			return;
		}
		if(super.saveClickX > 4 && super.saveClickY > 4 && super.saveClickX < 516 && super.saveClickY < 338) {
			int i1 = super.saveClickX - 4 - width / 2;
			if(i1 + width > 512)
				i1 = 512 - width;
			if(i1 < 0)
				i1 = 0;
			int l1 = super.saveClickY - 4;
			if(l1 + height > 334)
				l1 = 334 - height;
			if(l1 < 0)
				l1 = 0;
			menuOpen = true;
			menuScreenArea = 0;
			menuOffsetX = i1;
			menuOffsetY = l1;
			menuWidth = width;
			menuHeight = 15 * menuActionRow + 22;
		}
		if(super.saveClickX > 553 && super.saveClickY > 205 && super.saveClickX < 743 && super.saveClickY < 466) {
			int j1 = super.saveClickX - 553 - width / 2;
			if(j1 < 0)
				j1 = 0;
			else
				if(j1 + width > 190)
					j1 = 190 - width;
			int i2 = super.saveClickY - 205;
			if(i2 < 0)
				i2 = 0;
			else
				if(i2 + height > 261)
					i2 = 261 - height;
			menuOpen = true;
			menuScreenArea = 1;
			menuOffsetX = j1;
			menuOffsetY = i2;
			menuWidth = width;
			menuHeight = 15 * menuActionRow + 22;
		}
		if(super.saveClickX > 17 && super.saveClickY > 357 && super.saveClickX < 496 && super.saveClickY < 453) {
			int k1 = super.saveClickX - 17 - width / 2;
			if(k1 < 0)
				k1 = 0;
			else
				if(k1 + width > 479)
					k1 = 479 - width;
			int j2 = super.saveClickY - 357;
			if(j2 < 0)
				j2 = 0;
			else
				if(j2 + height > 96)
					j2 = 96 - height;
			menuOpen = true;
			menuScreenArea = 2;
			menuOffsetX = k1;
			menuOffsetY = j2;
			menuWidth = width;
			menuHeight = 15 * menuActionRow + 22;
		}
	}

	private void method117(ByteBuffer stream)
	{
		stream.initBitAccess();
		int j = stream.getBits(1);
		if(j == 0)
			return;
		int k = stream.getBits(2);
		if(k == 0)
		{
			anIntArray894[anInt893++] = myPlayerIndex;
			return;
		}
		if(k == 1)
		{
			int l = stream.getBits(3);
			myPlayer.move(false, l);
			int k1 = stream.getBits(1);
			if(k1 == 1)
				anIntArray894[anInt893++] = myPlayerIndex;
			return;
		}
		if(k == 2)
		{
			int i1 = stream.getBits(3);
			myPlayer.move(true, i1);
			int l1 = stream.getBits(3);
			myPlayer.move(true, l1);
			int j2 = stream.getBits(1);
			if(j2 == 1)
				anIntArray894[anInt893++] = myPlayerIndex;
			return;
		}
		if(k == 3)
		{
			floor_level = stream.getBits(2);
			int j1 = stream.getBits(1);
			int i2 = stream.getBits(1);
			if(i2 == 1)
				anIntArray894[anInt893++] = myPlayerIndex;
			int k2 = stream.getBits(7);
			int l2 = stream.getBits(7);
			myPlayer.setPos(l2, k2, j1 == 1);
		}
	}

	private void nullLoader()
	{
		aBoolean831 = false;
		while(drawingFlames)
		{
			aBoolean831 = false;
			try
			{
				Thread.sleep(50L);
			}
			catch(Exception _ex) { }
		}
		//titleBox = null;
		//titleButton = null;
		aBackgroundArray1152s = null;
		anIntArray850 = null;
		anIntArray851 = null;
		anIntArray852 = null;
		anIntArray853 = null;
		anIntArray1190 = null;
		anIntArray1191 = null;
		anIntArray828 = null;
		anIntArray829 = null;
		aClass30_Sub2_Sub1_Sub1_1201 = null;
		aClass30_Sub2_Sub1_Sub1_1202 = null;
	}

	private boolean method119(int i, int id) {
		boolean flag1 = false;
		RSInterface rsi = RSInterface.cache[id];
		if (rsi == null) {
			return false;
		}
		for(int index = 0; index < rsi.children.length; index++) {
			if(rsi.children[index] == -1) {
				break;
			}
			RSInterface child = RSInterface.cache[rsi.children[index]];
			if(child.type == 1) {
				flag1 |= method119(i, child.id);
			}
			if(child.type == 6 && (child.disabledAnimation != -1 || child.enabledAnimation != -1)) {
				boolean enabled = interfaceIsSelected(child);
				int anim;
				if(enabled) {
					anim = child.enabledAnimation;
				} else {
					anim = child.disabledAnimation;
				}
				if(anim != -1) {
					Sequence animation = Sequence.getSeq(anim);
					for(child.framesLeft += i; child.framesLeft > animation.method258(child.currentFrame);) {
						child.framesLeft -= animation.method258(child.currentFrame) + 1;
						child.currentFrame++;
						if(child.currentFrame >= animation.totalFrames) {
							child.currentFrame -= animation.frameStep;
							if(child.currentFrame < 0 || child.currentFrame >= animation.totalFrames) {
								child.currentFrame = 0;
							}
						}
						flag1 = true;
					}
				}
			}
		}
		return flag1;
	}

	private int method120()
	{
		int j = 3;
		if(yCameraCurve < 310)
		{
			int k = xCameraPos >> 7;
		int l = yCameraPos >> 7;
					int i1 = myPlayer.currentX >> 7;
				int j1 = myPlayer.currentY >> 7;
											if((byteGroundArray[floor_level][k][l] & 4) != 0)
												j = floor_level;
											int k1;
											if(i1 > k)
												k1 = i1 - k;
											else
												k1 = k - i1;
											int l1;
											if(j1 > l)
												l1 = j1 - l;
											else
												l1 = l - j1;
											if(k1 > l1)
											{
												int i2 = (l1 * 0x10000) / k1;
												int k2 = 32768;
												while(k != i1) 
												{
													if(k < i1)
														k++;
													else
														if(k > i1)
															k--;
													if((byteGroundArray[floor_level][k][l] & 4) != 0)
														j = floor_level;
													k2 += i2;
													if(k2 >= 0x10000)
													{
														k2 -= 0x10000;
														if(l < j1)
															l++;
														else
															if(l > j1)
																l--;
														if((byteGroundArray[floor_level][k][l] & 4) != 0)
															j = floor_level;
													}
												}
											} else
											{
												int j2 = (k1 * 0x10000) / l1;
												int l2 = 32768;
												while(l != j1) 
												{
													if(l < j1)
														l++;
													else
														if(l > j1)
															l--;
													if((byteGroundArray[floor_level][k][l] & 4) != 0)
														j = floor_level;
													l2 += j2;
													if(l2 >= 0x10000)
													{
														l2 -= 0x10000;
														if(k < i1)
															k++;
														else
															if(k > i1)
																k--;
														if((byteGroundArray[floor_level][k][l] & 4) != 0)
															j = floor_level;
													}
												}
											}
		}
		if((byteGroundArray[floor_level][myPlayer.currentX >> 7][myPlayer.currentY >> 7] & 4) != 0)
			j = floor_level;
		return j;
	}

	private int method121()
	{
		int j = method42(floor_level, yCameraPos, xCameraPos);
		if(j - zCameraPos < 800 && (byteGroundArray[floor_level][xCameraPos >> 7][yCameraPos >> 7] & 4) != 0)
			return floor_level;
		else
			return 3;
	}

	private void delIgnore(long l)
	{
		try
		{
			if(l == 0L)
				return;
			for(int j = 0; j < ignoreCount; j++)
				if(ignoreListAsLongs[j] == l)
				{
					ignoreCount--;
					redrawTabArea = true;
					System.arraycopy(ignoreListAsLongs, j + 1, ignoreListAsLongs, j, ignoreCount - j);

					out.putOpCode(74);
					out.putLong(l);
					return;
				}

			return;
		}
		catch(RuntimeException runtimeexception)
		{
			signlink.reportError("47229, " + 3 + ", " + l + ", " + runtimeexception.toString());
		}
		throw new RuntimeException();
	}

	public String getParameter(String s)
	{
		if(signlink.mainapp != null)
			return signlink.mainapp.getParameter(s);
		else
			return super.getParameter(s);
	}

	private void adjustVolume(boolean flag, int i)
	{
		signlink.midivol = i;
		if(flag)
			signlink.midi = "voladjust";
	}

	private int extractValue(RSInterface rsi, int config) {
		if(rsi.valueIndexArray == null || config >= rsi.valueIndexArray.length) {
			return -2;
		}
		try {
			int valueArray[] = rsi.valueIndexArray[config];
			int k = 0;
			int valuePointer = 0;
			int i1 = 0;
			do {
				int value = valueArray[valuePointer++];
				int returned = 0;
				byte byte0 = 0;
				switch (value) {
					case 0:
						return k;
					case 1:
						returned = currentStats[valueArray[valuePointer++]];
						break;
					case 2:
						returned = maxStats[valueArray[valuePointer++]];
						break;
					case 3:
						returned = currentExp[valueArray[valuePointer++]];
						break;
					case 4:
						RSInterface child = RSInterface.cache[valueArray[valuePointer++]];
						int k2 = valueArray[valuePointer++];
						if(k2 >= 0 && k2 < ItemDef.totalItems && (!ItemDef.getDef(k2).membersObject || isMembers)) {
							for(int j3 = 0; j3 < child.inventory.length; j3++) {
								if(child.inventory[j3] == k2 + 1) {
									returned += child.inventoryAmount[j3];
								}
							}
						}
						break;
					case 5:
						returned = variousSettings[valueArray[valuePointer++]];
						break;
					case 6:
						returned = anIntArray1019[maxStats[valueArray[valuePointer++]] - 1];
						break;
					case 7:
						returned = (variousSettings[valueArray[valuePointer++]] * 100) / 46875;
						break;
					case 8:
						returned = myPlayer.combatLevel;
						break;
					case 9:
						for(int index = 0; index < SkillConstants.total; index++) {
							if(SkillConstants.enabled[index]) {
								returned += maxStats[index];
							}
						}
						break;
					case 10:
						child = RSInterface.cache[valueArray[valuePointer++]];
						int l2 = valueArray[valuePointer++] + 1;
						if(l2 >= 0 && l2 < ItemDef.totalItems && (!ItemDef.getDef(l2).membersObject || isMembers)) {
							for(int k3 = 0; k3 < child.inventory.length; k3++) {
								if(child.inventory[k3] != l2) {
									continue;
								}
								returned = 0x3b9ac9ff;
								break;
							}
						}
						break;
					case 11:
						returned = energy;
						break;
					case 12:
						returned = weight;
						break;
					case 13:
						int i2 = variousSettings[valueArray[valuePointer++]];
						int i3 = valueArray[valuePointer++];
						returned = (i2 & 1 << i3) == 0 ? 0 : 1;
						break;
					case 14:
						int j2 = valueArray[valuePointer++];
						VarBit varBit = VarBit.cache[j2];
						int l3 = varBit.anInt648;
						int i4 = varBit.anInt649;
						int j4 = varBit.anInt650;
						int k4 = anIntArray1232[j4 - i4];
						returned = variousSettings[l3] >> i4 & k4;
						break;
					case 15:
						byte0 = 1;
						break;
					case 16:
						byte0 = 2;
						break;
					case 17:
						byte0 = 3;
						break;
					case 18:
						returned = (myPlayer.currentX >> 7) + baseX;
						break;
					case 19:
						returned = (myPlayer.currentY >> 7) + baseY;
						break;
					case 20:
						returned = valueArray[valuePointer++];
						break;

					default:
						break;
				}
				if(byte0 == 0) {
					if(i1 == 0)
						k += returned;
					if(i1 == 1)
						k -= returned;
					if(i1 == 2 && returned != 0)
						k /= returned;
					if(i1 == 3)
						k *= returned;
					i1 = 0;
				} else {
					i1 = byte0;
				}
			} while(true);
		} catch(Exception _ex) {
			return -1;
		}
	}

	private void drawTooltip()
	{
		if(menuActionRow < 2 && itemSelected == 0 && spellSelected == 0)
			return;
		String s;
		if(itemSelected == 1 && menuActionRow < 2)
			s = "Use " + selectedItemName + " with...";
		else
			if(spellSelected == 1 && menuActionRow < 2)
				s = spellTooltip + "...";
			else
				s = menuActionName[menuActionRow - 1];
		if(menuActionRow > 2)
			s = s + "@whi@ / " + (menuActionRow - 2) + " more options";
		bold.drawShadowedString(s, 4, 15, 0xffffff, currentTime / 1000);
	}

	private void drawMinimap()
	{
		mapArea.initDrawingArea();
		if(anInt1021 == 2)
		{
			byte abyte0[] = mapBack.myPixels;
			int ai[] = RSDrawingArea.pixels;
			int k2 = abyte0.length;
			for(int i5 = 0; i5 < k2; i5++)
				if(abyte0[i5] == 0)
					ai[i5] = 0;

			compass.shapeImageToPixels(33, minimapInt1, anIntArray1057, 256, anIntArray968, 25, 0, 0, 33, 25);
			gameArea.initDrawingArea();
			return;
		}
		int i = minimapInt1 + minimapInt2 & 0x7ff;
		int j = 48 + myPlayer.currentX / 32;
		int l2 = 464 - myPlayer.currentY / 32;
		aClass30_Sub2_Sub1_Sub1_1263.shapeImageToPixels(151, i, anIntArray1229, 256 + minimapInt3, anIntArray1052, l2, 5, 25, 146, j);
		compass.shapeImageToPixels(33, minimapInt1, anIntArray1057, 256, anIntArray968, 25, 0, 0, 33, 25);
		for(int j5 = 0; j5 < anInt1071; j5++)
		{
			int k = (anIntArray1072[j5] * 4 + 2) - myPlayer.currentX / 32;
			int i3 = (anIntArray1073[j5] * 4 + 2) - myPlayer.currentY / 32;
			markMinimap(aClass30_Sub2_Sub1_Sub1Array1140[j5], k, i3);
		}

		for(int k5 = 0; k5 < 104; k5++)
		{
			for(int l5 = 0; l5 < 104; l5++)
			{
				Deque class19 = groundArray[floor_level][k5][l5];
				if(class19 != null)
				{
					int l = (k5 * 4 + 2) - myPlayer.currentX / 32;
					int j3 = (l5 * 4 + 2) - myPlayer.currentY / 32;
					markMinimap(mapDotItem, l, j3);
				}
			}

		}

		for(int i6 = 0; i6 < npcCount; i6++)
		{
			NPC npc = npcArray[npcIndices[i6]];
			if(npc != null && npc.isVisible())
			{
				NPCDef entityDef = npc.desc;
				if(entityDef.childrenIDs != null)
					entityDef = entityDef.method161();
				if(entityDef != null && entityDef.aBoolean87 && entityDef.aBoolean84)
				{
					int i1 = npc.currentX / 32 - myPlayer.currentX / 32;
					int k3 = npc.currentY / 32 - myPlayer.currentY / 32;
					markMinimap(mapDotNPC, i1, k3);
				}
			}
		}

		for(int j6 = 0; j6 < playerCount; j6++)
		{
			Player player = playerArray[playerIndices[j6]];
			if(player != null && player.isVisible())
			{
				int j1 = player.currentX / 32 - myPlayer.currentX / 32;
				int l3 = player.currentY / 32 - myPlayer.currentY / 32;
				boolean flag1 = false;
				long l6 = TextUtils.longForName(player.name);
				for(int k6 = 0; k6 < friendsCount; k6++)
				{
					if(l6 != friendsListAsLongs[k6] || friendsNodeIDs[k6] == 0)
						continue;
					flag1 = true;
					break;
				}

				boolean flag2 = false;
				if(myPlayer.team != 0 && player.team != 0 && myPlayer.team == player.team)
					flag2 = true;
				if(flag1)
					markMinimap(mapDotFriend, j1, l3);
				else
					if(flag2)
						markMinimap(mapDotTeam, j1, l3);
					else
						markMinimap(mapDotPlayer, j1, l3);
			}
		}

		if(anInt855 != 0 && currentTime % 20 < 10)
		{
			if(anInt855 == 1 && anInt1222 >= 0 && anInt1222 < npcArray.length)
			{
				NPC class30_sub2_sub4_sub1_sub1_1 = npcArray[anInt1222];
				if(class30_sub2_sub4_sub1_sub1_1 != null)
				{
					int k1 = class30_sub2_sub4_sub1_sub1_1.currentX / 32 - myPlayer.currentX / 32;
					int i4 = class30_sub2_sub4_sub1_sub1_1.currentY / 32 - myPlayer.currentY / 32;
					method81(mapMarker, i4, k1);
				}
			}
			if(anInt855 == 2)
			{
				int l1 = ((anInt934 - baseX) * 4 + 2) - myPlayer.currentX / 32;
				int j4 = ((anInt935 - baseY) * 4 + 2) - myPlayer.currentY / 32;
				method81(mapMarker, j4, l1);
			}
			if(anInt855 == 10 && anInt933 >= 0 && anInt933 < playerArray.length)
			{
				Player class30_sub2_sub4_sub1_sub2_1 = playerArray[anInt933];
				if(class30_sub2_sub4_sub1_sub2_1 != null)
				{
					int i2 = class30_sub2_sub4_sub1_sub2_1.currentX / 32 - myPlayer.currentX / 32;
					int k4 = class30_sub2_sub4_sub1_sub2_1.currentY / 32 - myPlayer.currentY / 32;
					method81(mapMarker, k4, i2);
				}
			}
		}
		if(destX != 0)
		{
			int j2 = (destX * 4 + 2) - myPlayer.currentX / 32;
			int l4 = (destY * 4 + 2) - myPlayer.currentY / 32;
			markMinimap(mapFlag, j2, l4);
		}
		RSDrawingArea.drawFilledPixels(97, 78, 3, 3, 0xffffff);
		gameArea.initDrawingArea();
	}

	private void npcScreenPos(Entity entity, int i)
	{
		calcEntityScreenPos(entity.currentX, i, entity.currentY);

		//aryan	entity.entScreenX = spriteDrawX; entity.entScreenY = spriteDrawY;
	}

	private void calcEntityScreenPos(int i, int j, int l)
	{
		if(i < 128 || l < 128 || i > 13056 || l > 13056)
		{
			spriteDrawX = -1;
			spriteDrawY = -1;
			return;
		}
		int i1 = method42(floor_level, l, i) - j;
		i -= xCameraPos;
		i1 -= zCameraPos;
		l -= yCameraPos;
		int j1 = Model.SINE[yCameraCurve];
		int k1 = Model.COSINE[yCameraCurve];
		int l1 = Model.SINE[xCameraCurve];
		int i2 = Model.COSINE[xCameraCurve];
		int j2 = l * l1 + i * i2 >> 16;
			l = l * i2 - i * l1 >> 16;
		i = j2;
		j2 = i1 * k1 - l * j1 >> 16;
			l = i1 * j1 + l * k1 >> 16;
		i1 = j2;
		if(l >= 50)
		{
			spriteDrawX = Rasterizer.centerX + (i << 9) / l;
			spriteDrawY = Rasterizer.centerY + (i1 << 9) / l;
		} else
		{
			spriteDrawX = -1;
			spriteDrawY = -1;
		}
	}

	private void buildSplitPrivateChatMenu() {
		int offsetY = isFixed() ? 329 : getClientHeight() - 165;
		if(splitPrivateChat == 0)
			return;
		int i = 0;
		if(anInt1104 != 0) {
			i = 1;
		}
		for(int index = 0; index < 100; index++) {
			if(chatMessages[index] != null) {
				int type = chatTypes[index];
				String name = chatNames[index];
				if (name != null && name.indexOf("@") == 0) {
					name = name.substring(5);
				}
				if((type == 3 || type == 7) && (type == 7 || privateChatMode == 0 || privateChatMode == 1 && isFriendOrSelf(name))) {
					int y = offsetY - i * 13;
					if(super.mouseX > 4 && super.mouseY - 4 > y - (isFixed() ? 15 : 18) && super.mouseY - 4 <= y - (isFixed() ? 0 : 5)) {
						int width = regular.getEffectTextWidth("From:  " + name + chatMessages[index]) + 25;
						if(width > 450) {
							width = 450;
						}
						if(super.mouseX < 4 + width) {
							if(myPrivilege >= 1) {
								menuActionName[menuActionRow] = "Report abuse @whi@" + name;
								menuActionID[menuActionRow] = 2606;
								menuActionRow++;
							}
							menuActionName[menuActionRow] = "Add ignore @whi@" + name;
							menuActionID[menuActionRow] = 2042;
							menuActionRow++;
							menuActionName[menuActionRow] = "Add friend @whi@" + name;
							menuActionID[menuActionRow] = 2337;
							menuActionRow++;
						}
					}
					if(++i >= 5) {
						return;
					}
				}
				if((type == 5 || type == 6) && privateChatMode < 2 && ++i >= 5) {
					return;
				}
			}
		}
	}

	private void method130(int j, int k, int l, int i1, int j1, int k1,
			int l1, int i2, int j2)
	{
		GameObjectSpawnRequest class30_sub1 = null;
		for(GameObjectSpawnRequest class30_sub1_1 = (GameObjectSpawnRequest)deque.head(); class30_sub1_1 != null; class30_sub1_1 = (GameObjectSpawnRequest)deque.next())
		{
			if(class30_sub1_1.plane != l1 || class30_sub1_1.x != i2 || class30_sub1_1.y != j1 || class30_sub1_1.anInt1296 != i1)
				continue;
			class30_sub1 = class30_sub1_1;
			break;
		}

		if(class30_sub1 == null)
		{
			class30_sub1 = new GameObjectSpawnRequest();
			class30_sub1.plane = l1;
			class30_sub1.anInt1296 = i1;
			class30_sub1.x = i2;
			class30_sub1.y = j1;
			method89(class30_sub1);
			deque.append(class30_sub1);
		}
		class30_sub1.id2 = k;
		class30_sub1.type2 = k1;
		class30_sub1.face2 = l;
		class30_sub1.anInt1302 = j2;
		class30_sub1.anInt1294 = j;
	}

	private boolean interfaceIsSelected(RSInterface class9)
	{
		if(class9.valueCompareType == null)
			return false;
		for(int i = 0; i < class9.valueCompareType.length; i++)
		{
			int j = extractValue(class9, i);
			int k = class9.requiredValues[i];
			if(class9.valueCompareType[i] == 2)
			{
				if(j >= k)
					return false;
			} else
				if(class9.valueCompareType[i] == 3)
				{
					if(j <= k)
						return false;
				} else
					if(class9.valueCompareType[i] == 4)
					{
						if(j == k)
							return false;
					} else
						if(j != k)
							return false;
		}

		return true;
	}

	private DataInputStream openJagGrabInputStream(String s)
			throws IOException
			{
		//	   if(!aBoolean872)
		//		   if(signlink.mainapp != null)
		//			   return signlink.openurl(s);
		//		   else
		//			   return new DataInputStream((new URL(getCodeBase(), s)).openStream());
		if(aSocket832 != null)
		{
			try
			{
				aSocket832.close();
			}
			catch(Exception _ex) { }
			aSocket832 = null;
		}
		aSocket832 = openSocket(43595);
		aSocket832.setSoTimeout(10000);
		java.io.InputStream inputstream = aSocket832.getInputStream();
		OutputStream outputstream = aSocket832.getOutputStream();
		outputstream.write(("JAGGRAB /" + s + "\n\n").getBytes());
		return new DataInputStream(inputstream);
			}

	private void doFlamesDrawing()
	{
		char c = '\u0100';
		if(anInt1040 > 0)
		{
			for(int i = 0; i < 256; i++)
				if(anInt1040 > 768)
					anIntArray850[i] = method83(anIntArray851[i], anIntArray852[i], 1024 - anInt1040);
				else
					if(anInt1040 > 256)
						anIntArray850[i] = anIntArray852[i];
					else
						anIntArray850[i] = method83(anIntArray852[i], anIntArray851[i], 256 - anInt1040);

		} else
			if(anInt1041 > 0)
			{
				for(int j = 0; j < 256; j++)
					if(anInt1041 > 768)
						anIntArray850[j] = method83(anIntArray851[j], anIntArray853[j], 1024 - anInt1041);
					else
						if(anInt1041 > 256)
							anIntArray850[j] = anIntArray853[j];
						else
							anIntArray850[j] = method83(anIntArray853[j], anIntArray851[j], 256 - anInt1041);

			} else
			{
				System.arraycopy(anIntArray851, 0, anIntArray850, 0, 256);

			}
		System.arraycopy(aClass30_Sub2_Sub1_Sub1_1201.myPixels, 0, leftFlames.anIntArray315, 0, 33920);

		int i1 = 0;
		int j1 = 1152;
		for(int k1 = 1; k1 < c - 1; k1++)
		{
			int l1 = (anIntArray969[k1] * (c - k1)) / c;
			int j2 = 22 + l1;
			if(j2 < 0)
				j2 = 0;
			i1 += j2;
			for(int l2 = j2; l2 < 128; l2++)
			{
				int j3 = anIntArray828[i1++];
				if(j3 != 0)
				{
					int l3 = j3;
					int j4 = 256 - j3;
					j3 = anIntArray850[j3];
					int l4 = leftFlames.anIntArray315[j1];
					leftFlames.anIntArray315[j1++] = ((j3 & 0xff00ff) * l3 + (l4 & 0xff00ff) * j4 & 0xff00ff00) + ((j3 & 0xff00) * l3 + (l4 & 0xff00) * j4 & 0xff0000) >> 8;
				} else
				{
					j1++;
				}
			}

			j1 += j2;
		}

		//leftFlames.drawGraphics(0, 0, super.graphics);
		System.arraycopy(aClass30_Sub2_Sub1_Sub1_1202.myPixels, 0, rightFlames.anIntArray315, 0, 33920);

		i1 = 0;
		j1 = 1176;
		for(int k2 = 1; k2 < c - 1; k2++)
		{
			int i3 = (anIntArray969[k2] * (c - k2)) / c;
			int k3 = 103 - i3;
			j1 += i3;
			for(int i4 = 0; i4 < k3; i4++)
			{
				int k4 = anIntArray828[i1++];
				if(k4 != 0)
				{
					int i5 = k4;
					int j5 = 256 - k4;
					k4 = anIntArray850[k4];
					int k5 = rightFlames.anIntArray315[j1];
					rightFlames.anIntArray315[j1++] = ((k4 & 0xff00ff) * i5 + (k5 & 0xff00ff) * j5 & 0xff00ff00) + ((k4 & 0xff00) * i5 + (k5 & 0xff00) * j5 & 0xff0000) >> 8;
				} else
				{
					j1++;
				}
			}

			i1 += 128 - k3;
			j1 += 128 - k3 - i3;
		}

		//rightFlames.drawGraphics(637, 0, super.graphics);
	}

	private void method134(ByteBuffer stream)
	{
		int j = stream.getBits(8);
		if(j < playerCount)
		{
			for(int k = j; k < playerCount; k++)
				anIntArray840[anInt839++] = playerIndices[k];

		}
		if(j > playerCount)
		{
			signlink.reportError(myUsername + " Too many players");
			throw new RuntimeException("eek");
		}
		playerCount = 0;
		for(int l = 0; l < j; l++)
		{
			int i1 = playerIndices[l];
			Player player = playerArray[i1];
			int j1 = stream.getBits(1);
			if(j1 == 0)
			{
				playerIndices[playerCount++] = i1;
				player.time = currentTime;
			} else
			{
				int k1 = stream.getBits(2);
				if(k1 == 0)
				{
					playerIndices[playerCount++] = i1;
					player.time = currentTime;
					anIntArray894[anInt893++] = i1;
				} else
					if(k1 == 1)
					{
						playerIndices[playerCount++] = i1;
						player.time = currentTime;
						int l1 = stream.getBits(3);
						player.move(false, l1);
						int j2 = stream.getBits(1);
						if(j2 == 1)
							anIntArray894[anInt893++] = i1;
					} else
						if(k1 == 2)
						{
							playerIndices[playerCount++] = i1;
							player.time = currentTime;
							int i2 = stream.getBits(3);
							player.move(true, i2);
							int k2 = stream.getBits(3);
							player.move(true, k2);
							int l2 = stream.getBits(1);
							if(l2 == 1)
								anIntArray894[anInt893++] = i1;
						} else
							if(k1 == 3)
								anIntArray840[anInt839++] = i1;
			}
		}
	}

	public int LOGIN = 0;
	public int ACCOUNT = 1;
	public int CREATE = 2;

	int[] alpha = new int[]{ 0, 0, 0, 0, 0 };
	boolean[] increasing = new boolean[]{ false, false, false, false, false };
	
	public void handleGlow(int position) {
		int title_x = (getClientWidth() / 2) + 55;
		int title_y = (getClientHeight() / 2) - 230;
		int title_width = 300;
		int account_x = (getClientWidth() / 2) + 55;
		int account_y = (getClientHeight() / 2) - 230;
		int account_width = 300;
		int account_height = ((Accounts.accounts != null ? Accounts.accounts.length : 0) * 17) + 95;
		int[] rate = new int[]{ 8, 12 };
		if (position == 0) {
			if (alpha[loginCursorPos + 2] < 256 && increasing[loginCursorPos + 2]) {
				alpha[loginCursorPos + 2] += rate[0];
			}
			if (alpha[loginCursorPos + 2] == 256) {
				increasing[loginCursorPos + 2] = false;
			}
			if (alpha[loginCursorPos + 2] > 0 && !increasing[loginCursorPos + 2]) {
				alpha[loginCursorPos + 2] -= rate[0];
			}
			if (alpha[loginCursorPos + 2] <= 0) {
				increasing[loginCursorPos + 2] = true;
				alpha[loginCursorPos + 2] = 0;
			}
			field_hover.drawImage(title_x + 103, title_y + (loginCursorPos == 0 ? 48 : 78), alpha[loginCursorPos + 2]);
		} else if (position == 1) {
			if (mouseInRegion(title_x + (title_width / 2) - (button.myWidth + 3), title_x + (title_width / 2) - 3, title_y + 109, title_y + 131)) {
				alpha[0] += alpha[0] < 256 ? rate[1] : 0;
			} else {
				alpha[0] -= alpha[0] > 0 ? rate[1] : 0;
			}
			button_hover.drawImage(title_x + (title_width / 2) - (button.myWidth + 3), title_y + 109, alpha[0]);
			if (mouseInRegion(title_x + (title_width / 2) + 3, title_x + (title_width / 2) + 79, title_y + 109, title_y + 131)) {
				alpha[1] += alpha[1] < 256 ? rate[1] : 0;
			} else {
				alpha[1] -= alpha[1] > 0 ? rate[1] : 0;
			}
			button_hover.drawImage(title_x + (title_width / 2) + 3, title_y + 109, alpha[1]);
		} else if (position == 2) {
			if (mouseInRegion(account_x + (account_width / 2) - (button.myWidth / 2), account_x + (account_width / 2) + (button.myWidth / 2), (account_y + account_height) - 36, (account_y + account_height) - 14)) {
				alpha[4] += alpha[4] < 256 ? rate[1] : 0;
			} else {
				alpha[4] -= alpha[4] > 0 ? rate[1] : 0;
			}
			button_hover.drawImage(account_x + (account_width / 2) - (button.myWidth / 2), (account_y + account_height) - 36, alpha[4]);
		}
	}
	
	private void displayTitleScreen(boolean hideButtons) {
		int title_x = (getClientWidth() / 2) + 55;
		int title_y = (getClientHeight() / 2) - 230;
		int title_width = 300;
		int title_height = 145;
		int account_x = (getClientWidth() / 2) + 55;
		int account_y = (getClientHeight() / 2) - 230;
		int account_width = 300;
		int account_height = ((Accounts.accounts != null ? Accounts.accounts.length : 0) * 17) + 95;
		resetImageProducers();
		title.initDrawingArea();
		//RSDrawingArea.drawFilledPixels(0, 0, getClientWidth(), getClientHeight(), 0);
		background[0].drawImage((getClientWidth() / 2) - 382, (getClientHeight() / 2) - 251);
		background[1].drawImage(getClientWidth() / 2 + 1, (getClientHeight() / 2) - 251);
		background[2].drawImage((getClientWidth() / 2) - 382, getClientHeight() / 2 + 1);
		background[3].drawImage(getClientWidth() / 2 + 1, getClientHeight() / 2 + 1);
		if(loginScreenState == LOGIN) {
			RSDrawingArea.drawRoundedRectangle(title_x, title_y, title_width, title_height, 0, 120, true, false);
			if(loginMessage2.length() > 0) {
				fancy.drawCenteredString(loginMessage1, title_x + (title_width / 2), title_y + 22, 0xffffff, true);
				fancy.drawCenteredString(loginMessage2, title_x + (title_width / 2), title_y + 37, 0xffffff, true);
			} else {
				fancy.drawCenteredString(loginMessage1, title_x + (title_width / 2), title_y + 30, 0xffffff, true);
			}
			field.drawImage(title_x + 103, title_y + 48);
			field.drawImage(title_x + 103, title_y + 78);
			handleGlow(0);
			fancy.drawShadowedString("Username:", title_x + 28, title_y + 65, 0xffffff, true);
			fancy.drawShadowedString(getUsername() + ((loginCursorPos == 0) & (currentTime % 40 < 20) ? "@yel@|" : ""), title_x + 108, title_y + 65, 0xffffff, true);
			fancy.drawShadowedString("Password:", title_x + 31, title_y + 95, 0xffffff, true);
			fancy.drawShadowedString(TextUtils.passwordAsterisks(myPassword) + ((loginCursorPos == 1) & (currentTime % 40 < 20) ? "@yel@|" : ""), title_x + 108, title_y + 95, 0xffffff, true);
			if(!hideButtons) {
				button.drawImage(title_x + (title_width / 2) - (button.myWidth + 3), title_y + 109);
				button.drawImage(title_x + (title_width / 2) + 3, title_y + 109);
				handleGlow(1);
				fancy.drawCenteredString("Login", title_x + (title_width / 2) - (button.myWidth / 2) - 3, title_y + 125, 0xffffff, true);
				fancy.drawCenteredString("Accounts", title_x + (title_width / 2) + (button.myWidth / 2) + 3, title_y + 125, 0xffffff, true);
			}
		}
		if (loginScreenState == ACCOUNT) {
			RSDrawingArea.drawRoundedRectangle(account_x, account_y, account_width, account_height, 0, 120, true, false);
			if(loginMessage2.length() > 0) {
				fancy.drawCenteredString(loginMessage1, account_x + (account_width / 2), account_y + 22, 0xffffff, true);
				fancy.drawCenteredString(loginMessage2, account_x + (account_width / 2), account_y + 37, 0xffffff, true);
			} else {
				fancy.drawCenteredString(loginMessage1, account_x + (account_width / 2), account_y + 30, 0xffffff, true);
			}
			int text_x = account_x + (account_width / 2);
			int text_y = account_y + 57;
			if (Accounts.accounts != null) {
				for (int index = 0; index < Accounts.accounts.length; index++, text_y += 17) {
					if (Accounts.getAccounts()[index] != null) {
						fancy.drawCenteredString(Accounts.sortNamesByUsage()[index] + " (" + Accounts.getAccount(Accounts.sortNamesByUsage()[index]).uses + " use" + (Accounts.getAccount(Accounts.sortNamesByUsage()[index]).uses > 1 ? "s" : "") + ")", text_x, text_y, accountHover == index ? 0xffff00 : 0xffffff, true);
					}
				}
			}
			button.drawImage(account_x + (account_width / 2) - (button.myWidth / 2), (account_y + account_height) - 36);
			handleGlow(2);
			fancy.drawCenteredString("Back", account_x + (account_width / 2), (account_y + account_height) - 20, 0xffffff, true);
		}
		title.drawGraphics(0, 0, super.graphics);
	}
	public int accountHover = -1;

	private void drawFlames()
	{
		drawingFlames = true;
		try
		{
			long l = System.currentTimeMillis();
			int i = 0;
			int j = 20;
			while(aBoolean831) 
			{
				anInt1208++;
				calcFlamesPosition();
				calcFlamesPosition();
				doFlamesDrawing();
				if(++i > 10)
				{
					long l1 = System.currentTimeMillis();
					int k = (int)(l1 - l) / 10 - j;
					j = 40 - k;
					if(j < 5)
						j = 5;
					i = 0;
					l = l1;
				}
				try
				{
					Thread.sleep(j);
				}
				catch(Exception _ex) { }
			}
		}
		catch(Exception _ex) { }
		drawingFlames = false;
	}

	public void raiseWelcomeScreen()
	{
		welcomeScreenRaised = true;
	}

	private void method137(ByteBuffer stream, int j)
	{
		if(j == 84)
		{
			int k = stream.getUByte();
			int j3 = anInt1268 + (k >> 4 & 7);
			int i6 = anInt1269 + (k & 7);
			int l8 = stream.getShort();
			int k11 = stream.getShort();
			int l13 = stream.getShort();
			if(j3 >= 0 && i6 >= 0 && j3 < 104 && i6 < 104)
			{
				Deque class19_1 = groundArray[floor_level][j3][i6];
				if(class19_1 != null)
				{
					for(Item class30_sub2_sub4_sub2_3 = (Item)class19_1.head(); class30_sub2_sub4_sub2_3 != null; class30_sub2_sub4_sub2_3 = (Item)class19_1.next())
					{
						if(class30_sub2_sub4_sub2_3.ID != (l8 & 0x7fff) || class30_sub2_sub4_sub2_3.anInt1559 != k11)
							continue;
						class30_sub2_sub4_sub2_3.anInt1559 = l13;
						break;
					}

					spawnGroundItem(j3, i6);
				}
			}
			return;
		}
		if(j == 105)
		{
			int l = stream.getUByte();
			int k3 = anInt1268 + (l >> 4 & 7);
			int j6 = anInt1269 + (l & 7);
			int i9 = stream.getShort();
			int l11 = stream.getUByte();
			int i14 = l11 >> 4 & 0xf;
			int i16 = l11 & 7;
			if(myPlayer.pathX[0] >= k3 - i14 && myPlayer.pathX[0] <= k3 + i14 && myPlayer.pathY[0] >= j6 - i14 && myPlayer.pathY[0] <= j6 + i14 && aBoolean848 && !lowMem && anInt1062 < 50)
			{
				anIntArray1207[anInt1062] = i9;
				anIntArray1241[anInt1062] = i16;
				anIntArray1250[anInt1062] = Sounds.anIntArray326[i9];
				anInt1062++;
			}
		}
		if(j == 215)
		{
			int i1 = stream.method435();
			int l3 = stream.method428();
			int k6 = anInt1268 + (l3 >> 4 & 7);
			int j9 = anInt1269 + (l3 & 7);
			int i12 = stream.method435();
			int j14 = stream.getShort();
			if(k6 >= 0 && j9 >= 0 && k6 < 104 && j9 < 104 && i12 != unknownInt10)
			{
				Item class30_sub2_sub4_sub2_2 = new Item();
				class30_sub2_sub4_sub2_2.ID = i1;
				class30_sub2_sub4_sub2_2.anInt1559 = j14;
				if(groundArray[floor_level][k6][j9] == null)
					groundArray[floor_level][k6][j9] = new Deque();
				groundArray[floor_level][k6][j9].append(class30_sub2_sub4_sub2_2);
				spawnGroundItem(k6, j9);
			}
			return;
		}
		if(j == 156)
		{
			int j1 = stream.method426();
			int i4 = anInt1268 + (j1 >> 4 & 7);
			int l6 = anInt1269 + (j1 & 7);
			int k9 = stream.getShort();
			if(i4 >= 0 && l6 >= 0 && i4 < 104 && l6 < 104)
			{
				Deque class19 = groundArray[floor_level][i4][l6];
				if(class19 != null)
				{
					for(Item item = (Item)class19.head(); item != null; item = (Item)class19.next())
					{
						if(item.ID != (k9 & 0x7fff))
							continue;
						item.remove();
						break;
					}

					if(class19.head() == null)
						groundArray[floor_level][i4][l6] = null;
					spawnGroundItem(i4, l6);
				}
			}
			return;
		}
		if(j == 160)
		{
			int k1 = stream.method428();
			int j4 = anInt1268 + (k1 >> 4 & 7);
			int i7 = anInt1269 + (k1 & 7);
			int l9 = stream.method428();
			int j12 = l9 >> 2;
			int k14 = l9 & 3;
			int j16 = anIntArray1177[j12];
			int j17 = stream.method435();
			if(j4 >= 0 && i7 >= 0 && j4 < 103 && i7 < 103)
			{
				int j18 = intGroundArray[floor_level][j4][i7];
				int i19 = intGroundArray[floor_level][j4 + 1][i7];
				int l19 = intGroundArray[floor_level][j4 + 1][i7 + 1];
				int k20 = intGroundArray[floor_level][j4][i7 + 1];
				if(j16 == 0)
				{
					WallObject class10 = worldController.getWallObject(floor_level, j4, i7);
					if(class10 != null)
					{
						int k21 = class10.uid >> 14 & 0x7fff;
			if(j12 == 2)
			{
				class10.node1 = new ObjectOnTile(k21, 4 + k14, 2, i19, l19, j18, k20, j17, false);
				class10.node2 = new ObjectOnTile(k21, k14 + 1 & 3, 2, i19, l19, j18, k20, j17, false);
			} else
			{
				class10.node1 = new ObjectOnTile(k21, k14, j12, i19, l19, j18, k20, j17, false);
			}
					}
				}
				if(j16 == 1)
				{
					WallDecoration class26 = worldController.getWallDecoration(j4, i7, floor_level);
					if(class26 != null)
						class26.node = new ObjectOnTile(class26.uid >> 14 & 0x7fff, 0, 4, i19, l19, j18, k20, j17, false);
				}
				if(j16 == 2)
				{
					InteractableObject class28 = worldController.getInteractableObject(j4, i7, floor_level);
					if(j12 == 11)
						j12 = 10;
					if(class28 != null)
						class28.animable = new ObjectOnTile(class28.uid >> 14 & 0x7fff, k14, j12, i19, l19, j18, k20, j17, false);
				}
				if(j16 == 3) {
					GroundDecoration class49 = worldController.getGroundDecoration(i7, j4, floor_level);
					if(class49 != null)
						class49.animable = new ObjectOnTile(class49.uid >> 14 & 0x7fff, k14, 22, i19, l19, j18, k20, j17, false);
				}
			}
			return;
		}
		if(j == 147)
		{
			int l1 = stream.method428();
			int k4 = anInt1268 + (l1 >> 4 & 7);
			int j7 = anInt1269 + (l1 & 7);
			int i10 = stream.getShort();
			byte byte0 = stream.method430();
			int l14 = stream.method434();
			byte byte1 = stream.method429();
			int k17 = stream.getShort();
			int k18 = stream.method428();
			int j19 = k18 >> 2;
			int i20 = k18 & 3;
			int l20 = anIntArray1177[j19];
			byte byte2 = stream.getByte();
			int l21 = stream.getShort();
			byte byte3 = stream.method429();
			Player player;
			if(i10 == unknownInt10)
				player = myPlayer;
			else
				player = playerArray[i10];
			if(player != null)
			{
				ObjectDef class46 = ObjectDef.getDef(l21);
				int i22 = intGroundArray[floor_level][k4][j7];
				int j22 = intGroundArray[floor_level][k4 + 1][j7];
				int k22 = intGroundArray[floor_level][k4 + 1][j7 + 1];
				int l22 = intGroundArray[floor_level][k4][j7 + 1];
				Model model = class46.renderObject(j19, i20, i22, j22, k22, l22, -1);
				if(model != null)
				{
					method130(k17 + 1, -1, 0, l20, j7, 0, floor_level, k4, l14 + 1);
					player.anInt1707 = l14 + currentTime;
					player.anInt1708 = k17 + currentTime;
					player.aModel_1714 = model;
					int i23 = class46.sizeX;
					int j23 = class46.sizeY;
					if(i20 == 1 || i20 == 3)
					{
						i23 = class46.sizeY;
						j23 = class46.sizeX;
					}
					player.anInt1711 = k4 * 128 + i23 * 64;
					player.anInt1713 = j7 * 128 + j23 * 64;
					player.anInt1712 = method42(floor_level, player.anInt1713, player.anInt1711);
					if(byte2 > byte0)
					{
						byte byte4 = byte2;
						byte2 = byte0;
						byte0 = byte4;
					}
					if(byte3 > byte1)
					{
						byte byte5 = byte3;
						byte3 = byte1;
						byte1 = byte5;
					}
					player.anInt1719 = k4 + byte2;
					player.anInt1721 = k4 + byte0;
					player.anInt1720 = j7 + byte3;
					player.anInt1722 = j7 + byte1;
				}
			}
		}
		if(j == 151)
		{
			int i2 = stream.method426();
			int l4 = anInt1268 + (i2 >> 4 & 7);
			int k7 = anInt1269 + (i2 & 7);
			int j10 = stream.method434();
			int k12 = stream.method428();
			int i15 = k12 >> 2;
			int k16 = k12 & 3;
			int l17 = anIntArray1177[i15];
			if(l4 >= 0 && k7 >= 0 && l4 < 104 && k7 < 104)
				method130(-1, j10, k16, l17, k7, i15, floor_level, l4, 0);
			return;
		}
		if(j == 4)
		{
			int j2 = stream.getUByte();
			int i5 = anInt1268 + (j2 >> 4 & 7);
			int l7 = anInt1269 + (j2 & 7);
			int k10 = stream.getShort();
			int l12 = stream.getUByte();
			int j15 = stream.getShort();
			if(i5 >= 0 && l7 >= 0 && i5 < 104 && l7 < 104)
			{
				i5 = i5 * 128 + 64;
				l7 = l7 * 128 + 64;
				StillGraphics class30_sub2_sub4_sub3 = new StillGraphics(floor_level, currentTime, j15, k10, method42(floor_level, l7, i5) - l12, l7, i5);
				aClass19_1056.append(class30_sub2_sub4_sub3);
			}
			return;
		}
		if(j == 44)
		{
			int k2 = stream.method436();
			int j5 = stream.getShort();
			int i8 = stream.getUByte();
			int l10 = anInt1268 + (i8 >> 4 & 7);
			int i13 = anInt1269 + (i8 & 7);
			if(l10 >= 0 && i13 >= 0 && l10 < 104 && i13 < 104)
			{
				Item class30_sub2_sub4_sub2_1 = new Item();
				class30_sub2_sub4_sub2_1.ID = k2;
				class30_sub2_sub4_sub2_1.anInt1559 = j5;
				if(groundArray[floor_level][l10][i13] == null)
					groundArray[floor_level][l10][i13] = new Deque();
				groundArray[floor_level][l10][i13].append(class30_sub2_sub4_sub2_1);
				spawnGroundItem(l10, i13);
			}
			return;
		}
		if(j == 101)
		{
			int l2 = stream.method427();
			int k5 = l2 >> 2;
			int j8 = l2 & 3;
			int i11 = anIntArray1177[k5];
			int j13 = stream.getUByte();
			int k15 = anInt1268 + (j13 >> 4 & 7);
			int l16 = anInt1269 + (j13 & 7);
			if(k15 >= 0 && l16 >= 0 && k15 < 104 && l16 < 104)
				method130(-1, -1, j8, i11, l16, k5, floor_level, k15, 0);
			return;
		}
		if(j == 117)
		{
			int i3 = stream.getUByte();
			int l5 = anInt1268 + (i3 >> 4 & 7);
			int k8 = anInt1269 + (i3 & 7);
			int j11 = l5 + stream.getByte();
			int k13 = k8 + stream.getByte();
			int l15 = stream.getSignedShort();
			int i17 = stream.getShort();
			int i18 = stream.getUByte() * 4;
			int l18 = stream.getUByte() * 4;
			int k19 = stream.getShort();
			int j20 = stream.getShort();
			int i21 = stream.getUByte();
			int j21 = stream.getUByte();
			if(l5 >= 0 && k8 >= 0 && l5 < 104 && k8 < 104 && j11 >= 0 && k13 >= 0 && j11 < 104 && k13 < 104 && i17 != 65535)
			{
				l5 = l5 * 128 + 64;
				k8 = k8 * 128 + 64;
				j11 = j11 * 128 + 64;
				k13 = k13 * 128 + 64;
				Projectile class30_sub2_sub4_sub4 = new Projectile(i21, l18, k19 + currentTime, j20 + currentTime, j21, floor_level, method42(floor_level, k8, l5) - i18, k8, l5, l15, i17);
				class30_sub2_sub4_sub4.method455(k19 + currentTime, k13, method42(floor_level, k13, j11) - l18, j11);
				aClass19_1013.append(class30_sub2_sub4_sub4);
			}
		}
	}

	private static void setLowMem()
	{
		SceneGraph.lowMem = true;
		Rasterizer.lowMem = true;
		lowMem = true;
		MapRegion.lowMem = true;
		ObjectDef.lowMem = true;
	}

	private void method139(ByteBuffer stream)
	{
		stream.initBitAccess();
		int k = stream.getBits(8);
		if(k < npcCount)
		{
			for(int l = k; l < npcCount; l++)
				anIntArray840[anInt839++] = npcIndices[l];

		}
		if(k > npcCount)
		{
			signlink.reportError(getUsername() + " Too many npcs");
			throw new RuntimeException("eek");
		}
		npcCount = 0;
		for(int i1 = 0; i1 < k; i1++)
		{
			int j1 = npcIndices[i1];
			NPC npc = npcArray[j1];
			int k1 = stream.getBits(1);
			if(k1 == 0)
			{
				npcIndices[npcCount++] = j1;
				npc.time = currentTime;
			} else
			{
				int l1 = stream.getBits(2);
				if(l1 == 0)
				{
					npcIndices[npcCount++] = j1;
					npc.time = currentTime;
					anIntArray894[anInt893++] = j1;
				} else
					if(l1 == 1)
					{
						npcIndices[npcCount++] = j1;
						npc.time = currentTime;
						int i2 = stream.getBits(3);
						npc.move(false, i2);
						int k2 = stream.getBits(1);
						if(k2 == 1)
							anIntArray894[anInt893++] = j1;
					} else
						if(l1 == 2)
						{
							npcIndices[npcCount++] = j1;
							npc.time = currentTime;
							int j2 = stream.getBits(3);
							npc.move(true, j2);
							int l2 = stream.getBits(3);
							npc.move(true, l2);
							int i3 = stream.getBits(1);
							if(i3 == 1)
								anIntArray894[anInt893++] = j1;
						} else
							if(l1 == 3)
								anIntArray840[anInt839++] = j1;
			}
		}
	}

	private void processLoginScreenInput() {
		int title_x = (getClientWidth() / 2) + 55;
		int title_y = (getClientHeight() / 2) - 230;
		int title_width = 300;
		if(loginScreenState == LOGIN) {
			if (super.clickMode3 == 1 && clickInRegion(title_x, title_x + title_width, title_y + 48, title_y + 70)) {
				loginCursorPos = 0;
			}
			if (super.clickMode3 == 1 && clickInRegion(title_x, title_x + title_width, title_y + 78, title_y + 100)) {
				loginCursorPos = 1;
			}
			if (super.clickMode3 == 1 && clickInRegion(title_x + (title_width / 2) - 79, title_x + (title_width / 2) - 3, title_y + 109, title_y + 131)) {
				loginFailures = 0;
				login(getUsername(), myPassword, false, false);
				if(loggedIn)
					return;
			}
			if (super.clickMode3 == 1 && clickInRegion(title_x + (title_width / 2) + 3, title_x + (title_width / 2) + 79, title_y + 109, title_y + 131)) {
				loginMessage1 = "Please select an account:";
				loginScreenState = ACCOUNT;
			}
			do {
				int key = readCharacter();
				if(key == -1) {
					break;
				}
				boolean validKey = false;
				for(int index = 0; index < validUserPassChars.length(); index++) {
					if(key != validUserPassChars.charAt(index)) {
						continue;
					}
					validKey = true;
					break;
				}
				if(loginCursorPos == 0) {
					if(key == 8 && getUsername().length() > 0) {
						setUsername(myUsername.substring(0, myUsername.length() - 1));
					}
					if(key == 9 || key == 10 || key == 13) {
						loginCursorPos = 1;
					}
					if(validKey) {
						setUsername(myUsername + (char)key);
					}
					if(getUsername().length() > 12) {
						setUsername(myUsername.substring(0, 12));
					}
				} else if(loginCursorPos == 1) {
					if(key == 8 && myPassword.length() > 0) {
						myPassword = myPassword.substring(0, myPassword.length() - 1);
					}
					if (key == 9) {
						loginCursorPos = 0;
					}
					if(key == 10 || key == 13) {
						login(getUsername(), myPassword, false, false);
					}
					if(validKey) {
						myPassword += (char)key;
					}
					if(myPassword.length() > 20) {
						myPassword = myPassword.substring(0, 20);
					}
				}
			} while(true);
			return;
		}
		if (loginScreenState == ACCOUNT) {
			int account_x = (getClientWidth() / 2) + 55;
			int account_y = (getClientHeight() / 2) - 230;
			int account_width = 300;
			int account_height = ((Accounts.accounts != null ? Accounts.accounts.length : 0) * 17) + 95;
			int text_y = account_y + 57;
			if (Accounts.accounts != null) {
				accountHover = -1;
				for (int index = 0; index < Accounts.accounts.length; index++, text_y += 17) {
					if (mouseInRegion(account_x, account_x + account_width, text_y - 12, text_y)) {
						accountHover = index;
					}
					if (super.clickMode3 == 1 && clickInRegion(account_x, account_x + account_width, text_y - 12, text_y)) {
						loginFailures = 0;
						login(Accounts.getAccount(Accounts.sortNamesByUsage()[index]).name, Accounts.getAccount(Accounts.sortNamesByUsage()[index]).password, false, true);
						if(loggedIn) {
							return;
						}
					}
				}
			}
			if (super.clickMode3 == 1 && clickInRegion(account_x + (account_width / 2) - 38, account_x + (account_width / 2) + 38, (account_y + account_height) - 36, (account_y + account_height) - 14)) {
				loginMessage1 = "Please enter your login details.";
				loginMessage2 = "";
				loginScreenState = LOGIN;
			}
		}
	}

	public boolean mouseInRegion(int x1, int x2, int y1, int y2) {
		if (super.mouseX >= x1 && super.mouseX <= x2 && super.mouseY >= y1 && super.mouseY <= y2) {
			return true;
		}
		return false;
	}

	public boolean clickInRegion(int x1, int x2, int y1, int y2) {
		if (super.saveClickX >= x1 && super.saveClickX <= x2 && super.saveClickY >= y1 && super.saveClickY <= y2) {
			return true;
		}
		return false;
	}

	private void markMinimap(RSImage sprite, int i, int j)
	{
		int k = minimapInt1 + minimapInt2 & 0x7ff;
		int l = i * i + j * j;
		if(l > 6400)
			return;
		int i1 = Model.SINE[k];
		int j1 = Model.COSINE[k];
		i1 = (i1 * 256) / (minimapInt3 + 256);
		j1 = (j1 * 256) / (minimapInt3 + 256);
		int k1 = j * i1 + i * j1 >> 16;
		int l1 = j * j1 - i * i1 >> 16;
		if(l > 2500)
		{
			sprite.method354(mapBack, 83 - l1 - sprite.maxHeight / 2 - 4, ((94 + k1) - sprite.maxWidth / 2) + 4);
		} else
		{
			sprite.drawImage(((94 + k1) - sprite.maxWidth / 2) + 4, 83 - l1 - sprite.maxHeight / 2 - 4);
		}
	}

	private void method142(int i, int j, int k, int l, int i1, int j1, int k1
			)
	{
		if(i1 >= 1 && i >= 1 && i1 <= 102 && i <= 102)
		{
			if(lowMem && j != floor_level)
				return;
			int i2 = 0;
			if(j1 == 0)
				i2 = worldController.getWallObjectUID(j, i1, i);
			if(j1 == 1)
				i2 = worldController.getWallDecorationUID(j, i1, i);
			if(j1 == 2)
				i2 = worldController.getInteractableObjectUID(j, i1, i);
			if(j1 == 3)
				i2 = worldController.getGroundDecorationUID(j, i1, i);
			if(i2 != 0)
			{
				int i3 = worldController.getIdTagForPosition(j, i1, i, i2);
				int j2 = i2 >> 14 & 0x7fff;
		int k2 = i3 & 0x1f;
		int l2 = i3 >> 6;
		if(j1 == 0)
		{
			worldController.removeWallObject(i1, j, i);
			ObjectDef class46 = ObjectDef.getDef(j2);
			if(class46.isUnwalkable)
				aClass11Array1230[j].method215(l2, k2, class46.aBoolean757, i1, i);
		}
		if(j1 == 1)
			worldController.removeWallDecoration(i, j, i1);
		if(j1 == 2)
		{
			worldController.method293(j, i1, i);
			ObjectDef class46_1 = ObjectDef.getDef(j2);
			if(i1 + class46_1.sizeX > 103 || i + class46_1.sizeX > 103 || i1 + class46_1.sizeY > 103 || i + class46_1.sizeY > 103)
				return;
			if(class46_1.isUnwalkable)
				aClass11Array1230[j].method216(l2, class46_1.sizeX, i1, i, class46_1.sizeY, class46_1.aBoolean757);
		}
		if(j1 == 3)
		{
			worldController.removeGroundDecoration(j, i, i1);
			ObjectDef class46_2 = ObjectDef.getDef(j2);
			if(class46_2.isUnwalkable && class46_2.hasActions)
				aClass11Array1230[j].method218(i, i1);
		}
			}
			if(k1 >= 0)
			{
				int j3 = j;
				if(j3 < 3 && (byteGroundArray[1][i1][i] & 2) == 2)
					j3++;
				MapRegion.method188(worldController, k, i, l, j3, aClass11Array1230[j], intGroundArray, i1, k1, j);
			}
		}
	}

	private void updatePlayers(int i, ByteBuffer stream)
	{
		anInt839 = 0;
		anInt893 = 0;
		method117(stream);
		method134(stream);
		method91(stream, i);
		method49(stream);
		for(int k = 0; k < anInt839; k++)
		{
			int l = anIntArray840[k];
			if(playerArray[l].time != currentTime)
				playerArray[l] = null;
		}

		if(stream.offset != i)
		{
			signlink.reportError("Error packet size mismatch in getplayer pos:" + stream.offset + " psize:" + i);
			throw new RuntimeException("eek");
		}
		for(int i1 = 0; i1 < playerCount; i1++)
			if(playerArray[playerIndices[i1]] == null)
			{
				signlink.reportError(getUsername() + " null entry in pl list - pos:" + i1 + " size:" + playerCount);
				throw new RuntimeException("eek");
			}

	}

	private void setCameraPos(int j, int k, int l, int i1, int j1, int k1)
	{
		int l1 = 2048 - k & 0x7ff;
		int i2 = 2048 - j1 & 0x7ff;
		int j2 = 0;
		int k2 = 0;
		int l2 = j;
		if(l1 != 0)
		{
			int i3 = Model.SINE[l1];
			int k3 = Model.COSINE[l1];
			int i4 = k2 * k3 - l2 * i3 >> 16;
		l2 = k2 * i3 + l2 * k3 >> 16;
				k2 = i4;
		}
		if(i2 != 0)
		{
			/* xxx			if(cameratoggle){
				if(zoom == 0)
				zoom = k2;
			  if(lftrit == 0)
				lftrit = j2;
			  if(fwdbwd == 0)
				fwdbwd = l2;
			  k2 = zoom;
			  j2 = lftrit;
			  l2 = fwdbwd;
			}
			 */
			int j3 = Model.SINE[i2];
			int l3 = Model.COSINE[i2];
			int j4 = l2 * j3 + j2 * l3 >> 16;
			l2 = l2 * l3 - j2 * j3 >> 16;
			j2 = j4;
		}
		xCameraPos = l - j2;
		zCameraPos = i1 - k2;
		yCameraPos = k1 - l2;
		yCameraCurve = k;
		xCameraCurve = j1;
	}

	private boolean parsePacket()
	{
		if(socketStream == null)
			return false;
		try
		{
			int i = socketStream.available();
			if(i == 0)
				return false;
			if(opCode == -1)
			{
				socketStream.flushInputStream(in.buffer, 1);
				opCode = in.buffer[0] & 0xff;
				if(encryption != null)
					opCode = opCode - encryption.getNextKey() & 0xff;
				size = SizeConstants.packetSizes[opCode];
				i--;
			}
			if(size == -1)
				if(i > 0)
				{
					socketStream.flushInputStream(in.buffer, 1);
					size = in.buffer[0] & 0xff;
					i--;
				} else
				{
					return false;
				}
			if(size == -2)
				if(i > 1)
				{
					socketStream.flushInputStream(in.buffer, 2);
					in.offset = 0;
					size = in.getShort();
					i -= 2;
				} else
				{
					return false;
				}
			if(i < size)
				return false;
			in.offset = 0;
			socketStream.flushInputStream(in.buffer, size);
			anInt1009 = 0;
			anInt843 = anInt842;
			anInt842 = anInt841;
			anInt841 = opCode;
			if(opCode == 81)
			{
				updatePlayers(size, in);
				aBoolean1080 = false;
				opCode = -1;
				return true;
			}
			if(opCode == 176)
			{
				daysSinceRecovChange = in.method427();
				unreadMessages = in.method435();
				membersInt = in.getUByte();
				anInt1193 = in.method440();
				daysSinceLastLogin = in.getShort();
				if(anInt1193 != 0 && openInterfaceID == -1)
				{
					signlink.dnslookup(TextUtils.method586(anInt1193));
					clearTopInterfaces();
					char c = '\u028A';
					if(daysSinceRecovChange != 201 || membersInt == 1)
						c = '\u028F';
					reportAbuseInput = "";
					canMute = false;
					for(int k9 = 0; k9 < RSInterface.cache.length; k9++)
					{
						if(RSInterface.cache[k9] == null || RSInterface.cache[k9].contentType != c)
							continue;
						openInterfaceID = RSInterface.cache[k9].parentId;
						break;
					}

				}
				opCode = -1;
				return true;
			}
			if(opCode == 64)
			{
				anInt1268 = in.method427();
				anInt1269 = in.method428();
				for(int j = anInt1268; j < anInt1268 + 8; j++)
				{
					for(int l9 = anInt1269; l9 < anInt1269 + 8; l9++)
						if(groundArray[floor_level][j][l9] != null)
						{
							groundArray[floor_level][j][l9] = null;
							spawnGroundItem(j, l9);
						}

				}

				for(GameObjectSpawnRequest class30_sub1 = (GameObjectSpawnRequest)deque.head(); class30_sub1 != null; class30_sub1 = (GameObjectSpawnRequest)deque.next())
					if(class30_sub1.x >= anInt1268 && class30_sub1.x < anInt1268 + 8 && class30_sub1.y >= anInt1269 && class30_sub1.y < anInt1269 + 8 && class30_sub1.plane == floor_level)
						class30_sub1.anInt1294 = 0;

				opCode = -1;
				return true;
			}
			if(opCode == 185)
			{
				int k = in.method436();
				RSInterface.cache[k].disabledMediaType = 3;
				if(myPlayer.desc == null)
					RSInterface.cache[k].disabledMediaId = (myPlayer.anIntArray1700[0] << 25) + (myPlayer.anIntArray1700[4] << 20) + (myPlayer.equipment[0] << 15) + (myPlayer.equipment[8] << 10) + (myPlayer.equipment[11] << 5) + myPlayer.equipment[1];
				else
					RSInterface.cache[k].disabledMediaId = (int)(0x12345678L + myPlayer.desc.type);
				opCode = -1;
				return true;
			}
			if(opCode == 107)
			{
				aBoolean1160 = false;
				for(int l = 0; l < 5; l++)
					aBooleanArray876[l] = false;

				opCode = -1;
				return true;
			}
			if(opCode == 72)
			{
				int i1 = in.method434();
				RSInterface class9 = RSInterface.cache[i1];
				for(int k15 = 0; k15 < class9.inventory.length; k15++)
				{
					class9.inventory[k15] = -1;
					class9.inventory[k15] = 0;
				}

				opCode = -1;
				return true;
			}
			if(opCode == 214)
			{
				ignoreCount = size / 8;
				for(int j1 = 0; j1 < ignoreCount; j1++)
					ignoreListAsLongs[j1] = in.getLong();

				opCode = -1;
				return true;
			}
			if(opCode == 166)
			{
				aBoolean1160 = true;
				anInt1098 = in.getUByte();
				anInt1099 = in.getUByte();
				anInt1100 = in.getShort();
				anInt1101 = in.getUByte();
				anInt1102 = in.getUByte();
				if(anInt1102 >= 100)
				{
					xCameraPos = anInt1098 * 128 + 64;
					yCameraPos = anInt1099 * 128 + 64;
					zCameraPos = method42(floor_level, yCameraPos, xCameraPos) - anInt1100;
				}
				opCode = -1;
				return true;
			}
			if(opCode == 134)
			{
				redrawTabArea = true;
				int k1 = in.getUByte();
				int i10 = in.method439();
				int l15 = in.getUByte();
				currentExp[k1] = i10;
				currentStats[k1] = l15;
				maxStats[k1] = 1;
				for(int k20 = 0; k20 < 98; k20++)
					if(i10 >= anIntArray1019[k20])
						maxStats[k1] = k20 + 2;

				opCode = -1;
				return true;
			}
			if(opCode == 71)
			{
				int l1 = in.getShort();
				int j10 = in.method426();
				if(l1 == 65535)
					l1 = -1;
				tabInterfaceIDs[j10] = l1;
				redrawTabArea = true;
				tabAreaAltered = true;
				opCode = -1;
				return true;
			}
			if(opCode == 74)
			{
				int i2 = in.method434();
				if(i2 == 65535)
					i2 = -1;
				if(i2 != currentSong && musicEnabled && !lowMem && prevSong == 0)
				{
					nextSong = i2;
					songChanging = true;
					onDemandFetcher.method558(2, nextSong);
				}
				currentSong = i2;
				opCode = -1;
				return true;
			}
			if(opCode == 121)
			{
				int j2 = in.method436();
				int k10 = in.method435();
				if(musicEnabled && !lowMem)
				{
					nextSong = j2;
					songChanging = false;
					onDemandFetcher.method558(2, nextSong);
					prevSong = k10;
				}
				opCode = -1;
				return true;
			}
			if(opCode == 109)
			{
				resetLogout();
				opCode = -1;
				return false;
			}
			if(opCode == 70)
			{
				int k2 = in.getSignedShort();
				int l10 = in.method437();
				int i16 = in.method434();
				RSInterface class9_5 = RSInterface.cache[i16];
				class9_5.drawOffsetX = k2;
				class9_5.drawOffsetY = l10;
				opCode = -1;
				return true;
			}
			if(opCode == 73 || opCode == 241) {

				//mapReset();
				int l2 = anInt1069;
				int i11 = anInt1070;
				if(opCode == 73) {
					l2 = mapX = in.method435();
					i11 = mapY = in.getShort();
					aBoolean1159 = false;
				}
				if(opCode == 241)
				{
					i11 = in.method435();
					in.initBitAccess();
					for(int j16 = 0; j16 < 4; j16++)
					{
						for(int l20 = 0; l20 < 13; l20++)
						{
							for(int j23 = 0; j23 < 13; j23++)
							{
								int i26 = in.getBits(1);
								if(i26 == 1)
									anIntArrayArrayArray1129[j16][l20][j23] = in.getBits(26);
								else
									anIntArrayArrayArray1129[j16][l20][j23] = -1;
							}

						}

					}

					in.finishBitAccess();
					l2 = in.getShort();
					aBoolean1159 = true;
				}
				if(anInt1069 == l2 && anInt1070 == i11 && loadingStage == 2)
				{
					opCode = -1;
					return true;
				}
				anInt1069 = l2;
				anInt1070 = i11;
				baseX = (anInt1069 - 6) * 8;
				baseY = (anInt1070 - 6) * 8;
				aBoolean1141 = (anInt1069 / 8 == 48 || anInt1069 / 8 == 49) && anInt1070 / 8 == 48;
				if(anInt1069 / 8 == 48 && anInt1070 / 8 == 148)
					aBoolean1141 = true;
				loadingStage = 1;
				aLong824 = System.currentTimeMillis();
				gameArea.initDrawingArea();
				displayLoadingProgress("Loading - please wait.");
				gameArea.drawGraphics(getGameAreaX(), getGameAreaY(), super.graphics);
				if(opCode == 73)
				{
					int k16 = 0;
					for(int i21 = (anInt1069 - 6) / 8; i21 <= (anInt1069 + 6) / 8; i21++)
					{
						for(int k23 = (anInt1070 - 6) / 8; k23 <= (anInt1070 + 6) / 8; k23++)
							k16++;

					}

					aByteArrayArray1183 = new byte[k16][];
					aByteArrayArray1247 = new byte[k16][];
					anIntArray1234 = new int[k16];
					anIntArray1235 = new int[k16];
					anIntArray1236 = new int[k16];
					k16 = 0;
					for(int l23 = (anInt1069 - 6) / 8; l23 <= (anInt1069 + 6) / 8; l23++)
					{
						for(int j26 = (anInt1070 - 6) / 8; j26 <= (anInt1070 + 6) / 8; j26++)
						{
							anIntArray1234[k16] = (l23 << 8) + j26;
							if(aBoolean1141 && (j26 == 49 || j26 == 149 || j26 == 147 || l23 == 50 || l23 == 49 && j26 == 47))
							{
								anIntArray1235[k16] = -1;
								anIntArray1236[k16] = -1;
								k16++;
							} else
							{
								int k28 = anIntArray1235[k16] = onDemandFetcher.method562(0, j26, l23);
								if(k28 != -1)
									onDemandFetcher.method558(3, k28);
								int j30 = anIntArray1236[k16] = onDemandFetcher.method562(1, j26, l23);
								if(j30 != -1)
									onDemandFetcher.method558(3, j30);
								k16++;
							}
						}

					}

				}
				if(opCode == 241)
				{
					int l16 = 0;
					int ai[] = new int[676];
					for(int i24 = 0; i24 < 4; i24++)
					{
						for(int k26 = 0; k26 < 13; k26++)
						{
							for(int l28 = 0; l28 < 13; l28++)
							{
								int k30 = anIntArrayArrayArray1129[i24][k26][l28];
								if(k30 != -1)
								{
									int k31 = k30 >> 14 & 0x3ff;
							int i32 = k30 >> 3 & 0x7ff;
					int k32 = (k31 / 8 << 8) + i32 / 8;
					for(int j33 = 0; j33 < l16; j33++)
					{
						if(ai[j33] != k32)
							continue;
						k32 = -1;
						break;
					}

					if(k32 != -1)
						ai[l16++] = k32;
								}
							}

						}

					}

					aByteArrayArray1183 = new byte[l16][];
					aByteArrayArray1247 = new byte[l16][];
					anIntArray1234 = new int[l16];
					anIntArray1235 = new int[l16];
					anIntArray1236 = new int[l16];
					for(int l26 = 0; l26 < l16; l26++)
					{
						int i29 = anIntArray1234[l26] = ai[l26];
						int l30 = i29 >> 8 & 0xff;
					int l31 = i29 & 0xff;
					int j32 = anIntArray1235[l26] = onDemandFetcher.method562(0, l31, l30);
					if(j32 != -1)
						onDemandFetcher.method558(3, j32);
					int i33 = anIntArray1236[l26] = onDemandFetcher.method562(1, l31, l30);
					if(i33 != -1)
						onDemandFetcher.method558(3, i33);
					}

				}
				int i17 = baseX - anInt1036;
				int j21 = baseY - anInt1037;
				anInt1036 = baseX;
				anInt1037 = baseY;
				for(int j24 = 0; j24 < 16384; j24++)
				{
					NPC npc = npcArray[j24];
					if(npc != null)
					{
						for(int j29 = 0; j29 < 10; j29++)
						{
							npc.pathX[j29] -= i17;
							npc.pathY[j29] -= j21;
						}

						npc.currentX -= i17 * 128;
						npc.currentY -= j21 * 128;
					}
				}

				for(int i27 = 0; i27 < maxPlayers; i27++)
				{
					Player player = playerArray[i27];
					if(player != null)
					{
						for(int i31 = 0; i31 < 10; i31++)
						{
							player.pathX[i31] -= i17;
							player.pathY[i31] -= j21;
						}

						player.currentX -= i17 * 128;
						player.currentY -= j21 * 128;
					}
				}

				aBoolean1080 = true;
				byte byte1 = 0;
				byte byte2 = 104;
				byte byte3 = 1;
				if(i17 < 0)
				{
					byte1 = 103;
					byte2 = -1;
					byte3 = -1;
				}
				byte byte4 = 0;
				byte byte5 = 104;
				byte byte6 = 1;
				if(j21 < 0)
				{
					byte4 = 103;
					byte5 = -1;
					byte6 = -1;
				}
				for(int k33 = byte1; k33 != byte2; k33 += byte3)
				{
					for(int l33 = byte4; l33 != byte5; l33 += byte6)
					{
						int i34 = k33 + i17;
						int j34 = l33 + j21;
						for(int k34 = 0; k34 < 4; k34++)
							if(i34 >= 0 && j34 >= 0 && i34 < 104 && j34 < 104)
								groundArray[k34][k33][l33] = groundArray[k34][i34][j34];
							else
								groundArray[k34][k33][l33] = null;

					}

				}

				for(GameObjectSpawnRequest class30_sub1_1 = (GameObjectSpawnRequest)deque.head(); class30_sub1_1 != null; class30_sub1_1 = (GameObjectSpawnRequest)deque.next())
				{
					class30_sub1_1.x -= i17;
					class30_sub1_1.y -= j21;
					if(class30_sub1_1.x < 0 || class30_sub1_1.y < 0 || class30_sub1_1.x >= 104 || class30_sub1_1.y >= 104)
						class30_sub1_1.remove();
				}

				if(destX != 0)
				{
					destX -= i17;
					destY -= j21;
				}
				aBoolean1160 = false;
				opCode = -1;
				return true;
			}
			if(opCode == 208)
			{
				int i3 = in.method437();
				if(i3 >= 0)
					method60(i3);
				anInt1018 = i3;
				opCode = -1;
				return true;
			}
			if(opCode == 99)
			{
				anInt1021 = in.getUByte();
				opCode = -1;
				return true;
			}
			if(opCode == 75)
			{
				int j3 = in.method436();
				int j11 = in.method436();
				RSInterface.cache[j11].disabledMediaType = 2;
				RSInterface.cache[j11].disabledMediaId = j3;
				opCode = -1;
				return true;
			}
			if(opCode == 114)
			{
				anInt1104 = in.method434() * 30;
				opCode = -1;
				return true;
			}
			if(opCode == 60)
			{
				anInt1269 = in.getUByte();
				anInt1268 = in.method427();
				while(in.offset < size)
				{
					int k3 = in.getUByte();
					method137(in, k3);
				}
				opCode = -1;
				return true;
			}
			if(opCode == 35)
			{
				int l3 = in.getUByte();
				int k11 = in.getUByte();
				int j17 = in.getUByte();
				int k21 = in.getUByte();
				aBooleanArray876[l3] = true;
				anIntArray873[l3] = k11;
				anIntArray1203[l3] = j17;
				anIntArray928[l3] = k21;
				anIntArray1030[l3] = 0;
				opCode = -1;
				return true;
			}
			if(opCode == 174)
			{
				int i4 = in.getShort();
				int l11 = in.getUByte();
				int k17 = in.getShort();
				if(aBoolean848 && !lowMem && anInt1062 < 50)
				{
					anIntArray1207[anInt1062] = i4;
					anIntArray1241[anInt1062] = l11;
					anIntArray1250[anInt1062] = k17 + Sounds.anIntArray326[i4];
					anInt1062++;
				}
				opCode = -1;
				return true;
			}
			if(opCode == 104)
			{
				int j4 = in.method427();
				int i12 = in.method426();
				String s6 = in.getString();
				if(j4 >= 1 && j4 <= 5)
				{
					if(s6.equalsIgnoreCase("null"))
						s6 = null;
					atPlayerActions[j4 - 1] = s6;
					atPlayerArray[j4 - 1] = i12 == 0;
				}
				opCode = -1;
				return true;
			}
			if(opCode == 78)
			{
				destX = 0;
				opCode = -1;
				return true;
			}
			if(opCode == 253)
			{
				String s = in.getString();
				if(s.endsWith(":tradereq:"))
				{
					String s3 = s.substring(0, s.indexOf(":"));
					long l17 = TextUtils.longForName(s3);
					boolean flag2 = false;
					for(int j27 = 0; j27 < ignoreCount; j27++)
					{
						if(ignoreListAsLongs[j27] != l17)
							continue;
						flag2 = true;
						break;
					}

					if(!flag2 && anInt1251 == 0)
						pushMessage(s3, "wishes to trade with you.", 4);
				} else
					if(s.endsWith(":duelreq:"))
					{
						String s4 = s.substring(0, s.indexOf(":"));
						long l18 = TextUtils.longForName(s4);
						boolean flag3 = false;
						for(int k27 = 0; k27 < ignoreCount; k27++)
						{
							if(ignoreListAsLongs[k27] != l18)
								continue;
							flag3 = true;
							break;
						}

						if(!flag3 && anInt1251 == 0)
							pushMessage(s4, "wishes to duel with you.", 8);
					} else
						if(s.endsWith(":chalreq:"))
						{
							String s5 = s.substring(0, s.indexOf(":"));
							long l19 = TextUtils.longForName(s5);
							boolean flag4 = false;
							for(int l27 = 0; l27 < ignoreCount; l27++)
							{
								if(ignoreListAsLongs[l27] != l19)
									continue;
								flag4 = true;
								break;
							}

							if(!flag4 && anInt1251 == 0)
							{
								String s8 = s.substring(s.indexOf(":") + 1, s.length() - 9);
								pushMessage(s5, s8, 8);
							}
						} else
						{
							pushMessage("", s, 0);
						}
				opCode = -1;
				//serverMessage(s);

				return true;
			}
			if(opCode == 1)
			{
				for(int k4 = 0; k4 < playerArray.length; k4++)
					if(playerArray[k4] != null)
						playerArray[k4].anim = -1;

				for(int j12 = 0; j12 < npcArray.length; j12++)
					if(npcArray[j12] != null)
						npcArray[j12].anim = -1;

				opCode = -1;
				return true;
			}
			if(opCode == 50)
			{
				long l4 = in.getLong();
				int i18 = in.getUByte();
				String s7 = TextUtils.fixName(TextUtils.nameForLong(l4));
				for(int k24 = 0; k24 < friendsCount; k24++)
				{
					if(l4 != friendsListAsLongs[k24])
						continue;
					if(friendsNodeIDs[k24] != i18)
					{
						friendsNodeIDs[k24] = i18;
						redrawTabArea = true;
						if(i18 > 0)
							pushMessage("", s7 + " has logged in.", 5);
						if(i18 == 0)
							pushMessage("", s7 + " has logged out.", 5);
					}
					s7 = null;
					break;
				}

				if(s7 != null && friendsCount < 200)
				{
					friendsListAsLongs[friendsCount] = l4;
					friendsList[friendsCount] = s7;
					friendsNodeIDs[friendsCount] = i18;
					friendsCount++;
					redrawTabArea = true;
				}
				for(boolean flag6 = false; !flag6;)
				{
					flag6 = true;
					for(int k29 = 0; k29 < friendsCount - 1; k29++)
						if(friendsNodeIDs[k29] != nodeID && friendsNodeIDs[k29 + 1] == nodeID || friendsNodeIDs[k29] == 0 && friendsNodeIDs[k29 + 1] != 0)
						{
							int j31 = friendsNodeIDs[k29];
							friendsNodeIDs[k29] = friendsNodeIDs[k29 + 1];
							friendsNodeIDs[k29 + 1] = j31;
							String s10 = friendsList[k29];
							friendsList[k29] = friendsList[k29 + 1];
							friendsList[k29 + 1] = s10;
							long l32 = friendsListAsLongs[k29];
							friendsListAsLongs[k29] = friendsListAsLongs[k29 + 1];
							friendsListAsLongs[k29 + 1] = l32;
							redrawTabArea = true;
							flag6 = false;
						}

				}

				opCode = -1;
				return true;
			}
			if(opCode == 110)
			{
				if(tabID == 12)
					redrawTabArea = true;
				energy = in.getUByte();
				opCode = -1;
				return true;
			}
			if(opCode == 254) {
				anInt855 = in.getUByte();
				if(anInt855 == 1) {
					anInt1222 = in.getShort();
				}
				if(anInt855 >= 2 && anInt855 <= 6) {
					if(anInt855 == 2) {
						anInt937 = 64;
						anInt938 = 64;
					}
					if(anInt855 == 3) {
						anInt937 = 0;
						anInt938 = 64;
					}
					if(anInt855 == 4) {
						anInt937 = 128;
						anInt938 = 64;
					}
					if(anInt855 == 5) {
						anInt937 = 64;
						anInt938 = 0;
					}
					if(anInt855 == 6) {
						anInt937 = 64;
						anInt938 = 128;
					}
					anInt855 = 2;
					anInt934 = in.getShort();
					anInt935 = in.getShort();
					anInt936 = in.getUByte();
				}
				if(anInt855 == 10) {
					anInt933 = in.getShort();
				}
				opCode = -1;
				return true;
			}
			if(opCode == 248) {
				int open = in.method435();
				int overlay = in.getShort();
				if(backDialogID != -1) {
					backDialogID = -1;
					inputTaken = true;
				}
				if(dialogState != 0) {
					dialogState = 0;
					inputTaken = true;
				}
				openInterfaceID = open;
				invOverlayInterfaceID = overlay;
				redrawTabArea = true;
				tabAreaAltered = true;
				aBoolean1149 = false;
				opCode = -1;
				return true;
			}
			if(opCode == PacketConstants.getReceived().INTERFACE_SCROLLPOS_UPDATE) {
				int id = in.method434();
				int pos = in.method435();
				RSInterface rsi = RSInterface.cache[id];
				if(rsi != null && rsi.type == 0) {
					if(pos < 0) {
						pos = 0;
					}
					if(pos > rsi.scrollMax - rsi.height) {
						pos = rsi.scrollMax - rsi.height;
					}
					rsi.scrollPosition = pos;
				}
				opCode = -1;
				return true;
			}
			if(opCode == PacketConstants.getReceived().PACKET_68) {
				for(int index = 0; index < variousSettings.length; index++) {
					if(variousSettings[index] != anIntArray1045[index]) {
						variousSettings[index] = anIntArray1045[index];
						handleActions(index);
						redrawTabArea = true;
					}
				}
				opCode = -1;
				return true;
			}
			if(opCode == PacketConstants.getReceived().PACKET_196) {
				long name = in.getLong();
				int j18 = in.getInt();
				int rights = in.getUByte();
				boolean ignored = false;
				for(int index = 0; index < 100; index++) {
					if(anIntArray1240[index] != j18) {
						continue;
					}
					ignored = true;
					break;
				}
				if(rights <= 1) {
					for(int index = 0; index < ignoreCount; index++) {
						if(ignoreListAsLongs[index] != name) {
							continue;
						}
						ignored = true;
						break;
					}
				}
				if(!ignored && anInt1251 == 0) {
					try {
						anIntArray1240[anInt1169] = j18;
						anInt1169 = (anInt1169 + 1) % 100;
						String text = TextInput.method525(size - 13, in);
						text = Censor.censor(text);
						if (rights != 0) {
							pushMessage(getPrefix(rights) + TextUtils.fixName(TextUtils.nameForLong(name)), text, 7);
						} else {
							pushMessage(TextUtils.fixName(TextUtils.nameForLong(name)), text, 3);
						}
					} catch(Exception exception1) {
						signlink.reportError("cde1");
					}
				}
				opCode = -1;
				return true;
			}
			if(opCode == PacketConstants.getReceived().PACKET_85) {
				anInt1269 = in.method427();
				anInt1268 = in.method427();
				opCode = -1;
				return true;
			}
			if(opCode == PacketConstants.getReceived().PACKET_24) {
				anInt1054 = in.method428();
				if(anInt1054 == tabID) {
					if(anInt1054 == 3) {
						tabID = 1;
					} else {
						tabID = 3;
					}
					redrawTabArea = true;
				}
				opCode = -1;
				return true;
			}
			if(opCode == PacketConstants.getReceived().UPDATE_INTERFACE_MODEL_ZOOM) {
				int interface_id = in.method434();
				int zoom = in.getShort();
				int item_id = in.getShort();
				if(item_id == 65535) {
					RSInterface.cache[interface_id].disabledMediaType = 0;
					opCode = -1;
					return true;
				} else {
					ItemDef item = ItemDef.getDef(item_id);
					RSInterface.cache[interface_id].disabledMediaType = 4;
					RSInterface.cache[interface_id].disabledMediaId = item_id;
					RSInterface.cache[interface_id].rotationX = item.modelRotation1;
					RSInterface.cache[interface_id].rotationY = item.modelRotation2;
					RSInterface.cache[interface_id].zoom = (item.modelZoom * 100) / zoom;
					opCode = -1;
					return true;
				}
			}
			if(opCode == PacketConstants.getReceived().PACKET_171) {
				boolean flag1 = in.getUByte() == 1;
				int id = in.getShort();
				RSInterface.cache[id].showInterface = flag1;
				opCode = -1;
				return true;
			}
			if(opCode == PacketConstants.getReceived().SET_OVERLAY_INTERFACE) {
				int id = in.method434();
				method60(id);
				if(backDialogID != -1) {
					backDialogID = -1;
					inputTaken = true;
				}
				if(dialogState != 0) {
					dialogState = 0;
					inputTaken = true;
				}
				invOverlayInterfaceID = id;
				redrawTabArea = true;
				tabAreaAltered = true;
				openInterfaceID = -1;
				fullscreenInterfaceID = -1;
				aBoolean1149 = false;
				opCode = -1;
				return true;
			}
			if(opCode == PacketConstants.getReceived().INTERFACE_TEXT_UPDATE) {
				String text = in.getString();
				int id = in.method435();
				RSInterface.cache[id].disabledText = text;
				if(RSInterface.cache[id].parentId == tabInterfaceIDs[tabID]) {
					redrawTabArea = true;
				}
				opCode = -1;
				return true;
			}
			if(opCode == PacketConstants.getReceived().SET_CHAT_MODES) {
				publicChatMode = in.getUByte();
				privateChatMode = in.getUByte();
				tradeMode = in.getUByte();
				aBoolean1233 = true;
				inputTaken = true;
				opCode = -1;
				return true;
			}
			if(opCode == PacketConstants.getReceived().WEIGHT_UPDATE) {
				if(tabID == 12) {
					redrawTabArea = true;
				}
				weight = in.getSignedShort();
				opCode = -1;
				return true;
			}
			if(opCode == PacketConstants.getReceived().INTERFACE_MEDIA_UPDATE) {
				int id = in.method436();
				int media = in.getShort();
				RSInterface.cache[id].disabledMediaType = 1;
				RSInterface.cache[id].disabledMediaId = media;
				opCode = -1;
				return true;
			}
			if(opCode == PacketConstants.getReceived().INTERFACE_TEXT_COLOR_UPDATE) {
				int id = in.method436();
				int hex = in.method436();
				int r = hex >> 10 & 0x1f;
				int g = hex >> 5 & 0x1f;
				int b = hex & 0x1f;
				RSInterface.cache[id].disabledColor = (r << 19) + (g << 11) + (b << 3);
				opCode = -1;
				return true;
			}
			if(opCode == PacketConstants.getReceived().ALL_ITEMS_UPDATE) {
				redrawTabArea = true;
				int id = in.getShort();
				RSInterface rsi = RSInterface.cache[id];
				int total = in.getShort();
				for(int index = 0; index < total; index++) {
					int amount = in.getUByte();
					if(amount == 255) {
						amount = in.method440();
					}
					rsi.inventory[index] = in.method436();
					rsi.inventoryAmount[index] = amount;
				}
				for(int index = total; index < rsi.inventory.length; index++) {
					rsi.inventory[index] = 0;
					rsi.inventoryAmount[index] = 0;
				}
				opCode = -1;
				return true;
			}
			if(opCode == PacketConstants.getReceived().SINGLE_ITEM_UPDATE) {
				redrawTabArea = true;
				int id = in.getShort();
				RSInterface rsi = RSInterface.cache[id];
				while(in.offset < size) {
					int index = in.method422();
					int item_id = in.getShort();
					int amount = in.getUByte();
					if(amount == 255) {
						amount = in.getInt();
					}
					if(index >= 0 && index < rsi.inventory.length) {
						rsi.inventory[index] = item_id;
						rsi.inventoryAmount[index] = amount;
					}
				}
				opCode = -1;
				return true;
			}
			if(opCode == PacketConstants.getReceived().INTERFACE_MODEL_UPDATE) {
				int zoom = in.method435();
				int id = in.getShort();
				int rotation1 = in.getShort();
				int rotation2 = in.method436();
				RSInterface.cache[id].rotationX = rotation1;
				RSInterface.cache[id].rotationY = rotation2;
				RSInterface.cache[id].zoom = zoom;
				opCode = -1;
				return true;
			}
			if(opCode == PacketConstants.getReceived().PACKET_221) {
				anInt900 = in.getUByte();
				redrawTabArea = true;
				opCode = -1;
				return true;
			}
			if(opCode == 177) {
				aBoolean1160 = true;
				anInt995 = in.getUByte();
				anInt996 = in.getUByte();
				anInt997 = in.getShort();
				anInt998 = in.getUByte();
				anInt999 = in.getUByte();
				if(anInt999 >= 100) {
					int k7 = anInt995 * 128 + 64;
					int k14 = anInt996 * 128 + 64;
					int i20 = method42(floor_level, k14, k7) - anInt997;
					int l22 = k7 - xCameraPos;
					int k25 = i20 - zCameraPos;
					int j28 = k14 - yCameraPos;
					int i30 = (int)Math.sqrt(l22 * l22 + j28 * j28);
					yCameraCurve = (int)(Math.atan2(k25, i30) * 325.94900000000001D) & 0x7ff;
					xCameraCurve = (int)(Math.atan2(l22, j28) * -325.94900000000001D) & 0x7ff;
					if(yCameraCurve < 128) {
						yCameraCurve = 128;
					}
					if(yCameraCurve > 383) {
						yCameraCurve = 383;
					}
				}
				opCode = -1;
				return true;
			}
			if(opCode == PacketConstants.getReceived().PACKET_249) {
				anInt1046 = in.method426();
				unknownInt10 = in.method436();
				opCode = -1;
				return true;
			}
			if(opCode == PacketConstants.getReceived().NPC_UPDATE) {
				try {
					updateNPCs(in, size);
				} catch (Exception e) {
					e.printStackTrace();
				}
				opCode = -1;
				return true;
			}
			if(opCode == PacketConstants.getReceived().SHOW_NUMERIC_INPUT) {
				promptRaised = false;
				dialogState = 1;
				amountOrNameInput = "";
				inputTaken = true;
				opCode = -1;
				return true;
			}
			if(opCode == PacketConstants.getReceived().SHOW_STRING_INPUT) {
				promptRaised = false;
				dialogState = 2;
				amountOrNameInput = "";
				inputTaken = true;
				opCode = -1;
				return true;
			}
			if(opCode == PacketConstants.getReceived().SET_OPEN_INTERFACE) {
				int id = in.getShort();
				method60(id);
				if(invOverlayInterfaceID != -1) {
					invOverlayInterfaceID = -1;
					redrawTabArea = true;
					tabAreaAltered = true;
				}
				if(backDialogID != -1) {
					backDialogID = -1;
					inputTaken = true;
				}
				if(dialogState != 0) {
					dialogState = 0;
					inputTaken = true;
				}
				openInterfaceID = id;
				aBoolean1149 = false;
				opCode = -1;
				return true;
			}
			if(opCode == PacketConstants.getReceived().SET_DIALOG_ID) {
				int id = in.method438();
				dialogID = id;
				inputTaken = true;
				opCode = -1;
				return true;
			}
			if(opCode == 87) {
				int id = in.method434();
				int state = in.method439();
				anIntArray1045[id] = state;
				if(variousSettings[id] != state) {
					variousSettings[id] = state;
					handleActions(id);
					redrawTabArea = true;
					if(dialogID != -1) {
						inputTaken = true;
					}
				}
				opCode = -1;
				return true;
			}
			if(opCode == 36) {
				int id = in.method434();
				byte state = in.getByte();
				anIntArray1045[id] = state;
				if(variousSettings[id] != state) {
					variousSettings[id] = state;
					handleActions(id);
					redrawTabArea = true;
					if(dialogID != -1) {
						inputTaken = true;
					}
				}
				opCode = -1;
				return true;
			}
			if(opCode == 61) {
				anInt1055 = in.getUByte();
				opCode = -1;
				return true;
			}
			if(opCode == 200) {
				int id = in.getShort();
				int i15 = in.getSignedShort();
				RSInterface rsi = RSInterface.cache[id];
				rsi.disabledAnimation = i15;
				if(i15 == -1) {
					rsi.currentFrame = 0;
					rsi.framesLeft = 0;
				}
				opCode = -1;
				return true;
			}
			if(opCode == PacketConstants.getReceived().CLOSE_INTERFACES) {
				if(invOverlayInterfaceID != -1) {
					invOverlayInterfaceID = -1;
					redrawTabArea = true;
					tabAreaAltered = true;
				}
				if(backDialogID != -1) {
					backDialogID = -1;
					inputTaken = true;
				}
				if(dialogState != 0) {
					dialogState = 0;
					inputTaken = true;
				}
				openInterfaceID = -1;
				fullscreenInterfaceID = -1;
				aBoolean1149 = false;
				opCode = -1;
				return true;
			}
			if(opCode == 105 || opCode == 84 || opCode == 147 || opCode == 215 || opCode == 4 || opCode == 117 || opCode == 156 || opCode == 44 || opCode == 160 || opCode == 101 || opCode == 151)
			{
				method137(in, opCode);
				opCode = -1;
				return true;
			}
			if(opCode == 106)
			{
				tabID = in.method427();
				redrawTabArea = true;
				tabAreaAltered = true;
				opCode = -1;
				return true;
			}
			if(opCode == 164)
			{
				int j9 = in.method434();
				method60(j9);
				if(invOverlayInterfaceID != -1)
				{
					invOverlayInterfaceID = -1;
					redrawTabArea = true;
					tabAreaAltered = true;
				}
				backDialogID = j9;
				inputTaken = true;
				openInterfaceID = -1;
				fullscreenInterfaceID = -1;
				aBoolean1149 = false;
				opCode = -1;
				return true;
			}
			//signlink.reporterror("T1 - " + opCode + "," + size + " - " + anInt842 + "," + anInt843);
			signlink.reportError("(Unknown opCode) opCode: " + opCode + ", size: " + size + ", prevOpCode: " + anInt842 + ", thirdOpCode: " + anInt843);
			resetLogout();
		} catch(IOException _ex) {
			dropClient();
		}
		catch(Exception exception) {
			String error = "(Handling exception) opCode: " + opCode + ", prevOpCode: " + anInt842 + ", thirdOpCode: " + anInt842 + ", size: " + size + ", xPos: " + (baseX + myPlayer.pathX[0]) + ", yPos: " + (baseY + myPlayer.pathY[0]);
			//String error = "T2 - " + opCode + "," + anInt842 + "," + anInt843 + " - " + size + "," + (baseX + myPlayer.smallX[0]) + "," + (baseY + myPlayer.smallY[0]) + " - ";
			for(int index = 0; index < size && index < 50; index++) {
				error = error + in.buffer[index] + ",";
			}
			signlink.reportError(error);
			resetLogout();
		}
		return true;
	}

	public void displayLoadingProgress(String string) {
		int width = regular.getTextWidth(string);
		int height = 22;
		int newLineIndex = string.indexOf("\\n");
		String line1;
		String line2;
		if(newLineIndex != -1) {
			line1 = string.substring(0, newLineIndex);
			line2 = string.substring(newLineIndex + 2);
			if (regular.getTextWidth(line1) > regular.getTextWidth(line2)) {
				width = regular.getTextWidth(line1);
			} else {
				width = regular.getTextWidth(line2);
			}
			height = 35;
		} else {
			line1 = string;
			line2 = "";
		}
		RSDrawingArea.drawFilledPixels(2, 2, width + 8, height, 0);
		RSDrawingArea.drawUnfilledPixels(2, 2, width + 8, height, 0xFFFFFF);
		regular.drawCenteredString(line1, 6 + (width / 2), 17, 0xFFFFFF, false);
		if (line2.length() > 0) {
			regular.drawCenteredString(line2, 6 + (width / 2), 30, 0xFFFFFF, false);
		}
	}

	private void method146()
	{
		anInt1265++;
		method47(true);
		method26(true);
		method47(false);
		method26(false);
		method55();
		method104();
		if(!aBoolean1160)
		{
			int i = anInt1184;
			if(anInt984 / 256 > i)
				i = anInt984 / 256;
			if(aBooleanArray876[4] && anIntArray1203[4] + 128 > i)
				i = anIntArray1203[4] + 128;
			int k = minimapInt1 + anInt896 & 0x7ff;
			setCameraPos(600 + i * 3, i, anInt1014, method42(floor_level, myPlayer.currentY, myPlayer.currentX) - 50, k, anInt1015);
		}
		int j;
		if(!aBoolean1160)
			j = method120();
		else
			j = method121();
		int l = xCameraPos;
		int i1 = zCameraPos;
		int j1 = yCameraPos;
		int k1 = yCameraCurve;
		int l1 = xCameraCurve;
		for(int i2 = 0; i2 < 5; i2++)
			if(aBooleanArray876[i2])
			{
				int j2 = (int)((Math.random() * (double)(anIntArray873[i2] * 2 + 1) - (double)anIntArray873[i2]) + Math.sin((double)anIntArray1030[i2] * ((double)anIntArray928[i2] / 100D)) * (double)anIntArray1203[i2]);
				if(i2 == 0)
					xCameraPos += j2;
				if(i2 == 1)
					zCameraPos += j2;
				if(i2 == 2)
					yCameraPos += j2;
				if(i2 == 3)
					xCameraCurve = xCameraCurve + j2 & 0x7ff;
				if(i2 == 4)
				{
					yCameraCurve += j2;
					if(yCameraCurve < 128)
						yCameraCurve = 128;
					if(yCameraCurve > 383)
						yCameraCurve = 383;
				}
			}

		int totalTextures = Rasterizer.textureGetCount;
		Model.aBoolean1684 = true;
		Model.anInt1687 = 0;
		Model.anInt1685 = super.mouseX - 4;
		Model.anInt1686 = super.mouseY - 4;
		RSDrawingArea.setAllPixelsToZero();
		worldController.render(xCameraPos, yCameraPos, xCameraCurve, zCameraPos, j, yCameraCurve);
		worldController.clearInteractableObjectCache();
		updateEntities();
		drawHeadIcon();
		handleTextureMovement(totalTextures);
		if (!isFixed()) {
			drawChatArea();
			drawTabArea();
		}
		draw3dScreen();
		gameArea.drawGraphics(getGameAreaX(), getGameAreaY(), super.graphics);
		xCameraPos = l;
		zCameraPos = i1;
		yCameraPos = j1;
		yCameraCurve = k1;
		xCameraCurve = l1;
	}

	private void clearTopInterfaces()
	{
		out.putOpCode(130);
		if(invOverlayInterfaceID != -1)
		{
			invOverlayInterfaceID = -1;
			redrawTabArea = true;
			aBoolean1149 = false;
			tabAreaAltered = true;
		}
		if(backDialogID != -1)
		{
			backDialogID = -1;
			inputTaken = true;
			aBoolean1149 = false;
		}
		openInterfaceID = -1;
		fullscreenInterfaceID = -1;
	}

	private Client() {
		loginScreenState = LOGIN;
		loginMessage1 = "Please enter your login details.";
		loginMessage2 = "";
		anIntArrayArray825 = new int[104][104];
		friendsNodeIDs = new int[200];
		groundArray = new Deque[4][104][104];
		aBoolean831 = false;
		aStream_834 = new ByteBuffer(new byte[5000]);
		npcArray = new NPC[16384];
		npcIndices = new int[16384];
		anIntArray840 = new int[1000];
		aStream_847 = ByteBuffer.create();
		aBoolean848 = true;
		openInterfaceID = -1;
		fullscreenInterfaceID = -1;
		currentExp = new int[SkillConstants.total];
		aBoolean872 = false;
		anIntArray873 = new int[5];
		anInt874 = -1;
		aBooleanArray876 = new boolean[5];
		drawFlames = false;
		reportAbuseInput = "";
		unknownInt10 = -1;
		menuOpen = false;
		inputString = "";
		maxPlayers = 2048;
		myPlayerIndex = 2047;
		playerArray = new Player[maxPlayers];
		playerIndices = new int[maxPlayers];
		anIntArray894 = new int[maxPlayers];
		aStreamArray895s = new ByteBuffer[maxPlayers];
		anInt897 = 1;
		anIntArrayArray901 = new int[104][104];
		anInt902 = 0x766654;
		texturePixels = new byte[16384];
		currentStats = new int[SkillConstants.total];
		ignoreListAsLongs = new long[100];
		loadingError = false;
		anInt927 = 0x332d25;
		anIntArray928 = new int[5];
		anIntArrayArray929 = new int[104][104];
		chatTypes = new int[100];
		chatNames = new String[100];
		chatMessages = new String[100];
		sideIcons = new IndexedImage[13];
		aBoolean954 = true;
		friendsListAsLongs = new long[200];
		currentSong = -1;
		drawingFlames = false;
		spriteDrawX = -1;
		spriteDrawY = -1;
		anIntArray968 = new int[33];
		anIntArray969 = new int[256];
		jagCache = new ResourceCache[5];
		variousSettings = new int[2000];
		aBoolean972 = false;
		anInt975 = 50;
		anIntArray976 = new int[anInt975];
		anIntArray977 = new int[anInt975];
		anIntArray978 = new int[anInt975];
		anIntArray979 = new int[anInt975];
		anIntArray980 = new int[anInt975];
		anIntArray981 = new int[anInt975];
		anIntArray982 = new int[anInt975];
		aStringArray983 = new String[anInt975];
		anInt985 = -1;
		hitMarks = new RSImage[20];
		anIntArray990 = new int[5];
		aBoolean994 = false;
		anInt1002 = 0x23201b;
		amountOrNameInput = "";
		aClass19_1013 = new Deque();
		aBoolean1017 = false;
		anInt1018 = -1;
		anIntArray1030 = new int[5];
		aBoolean1031 = false;
		mapFunctions = new RSImage[100];
		dialogID = -1;
		maxStats = new int[SkillConstants.total];
		anIntArray1045 = new int[2000];
		aBoolean1047 = true;
		anIntArray1052 = new int[151];
		anInt1054 = -1;
		aClass19_1056 = new Deque();
		anIntArray1057 = new int[33];
		rsi = new RSInterface();
		mapScenes = new IndexedImage[100];
		anInt1063 = 0x4d4233;
		anIntArray1065 = new int[7];
		anIntArray1072 = new int[1000];
		anIntArray1073 = new int[1000];
		aBoolean1080 = false;
		friendsList = new String[200];
		in = ByteBuffer.create();
		expectedCRCs = new int[9];
		menuActionCmd2 = new int[500];
		menuActionCmd3 = new int[500];
		menuActionID = new int[500];
		menuActionCmd1 = new int[500];
		headIcons = new RSImage[20];
		background = new RSImage[4];
		tabAreaAltered = false;
		promptMessage = "";
		atPlayerActions = new String[5];
		atPlayerArray = new boolean[5];
		anIntArrayArrayArray1129 = new int[4][13][13];
		anInt1132 = 2;
		aClass30_Sub2_Sub1_Sub1Array1140 = new RSImage[1000];
		aBoolean1141 = false;
		aBoolean1149 = false;
		crosses = new RSImage[8];
		musicEnabled = true;
		redrawTabArea = false;
		loggedIn = false;
		canMute = false;
		aBoolean1159 = false;
		aBoolean1160 = false;
		anInt1171 = 1;
		myUsername = "";
		myPassword = "";
		genericLoadingError = false;
		reportAbuseInterfaceID = -1;
		deque = new Deque();
		anInt1184 = 128;
		invOverlayInterfaceID = -1;
		out = ByteBuffer.create();
		menuActionName = new String[500];
		anIntArray1203 = new int[5];
		anIntArray1207 = new int[50];
		anInt1210 = 2;
		anInt1211 = 78;
		promptInput = "";
		modIcons = new IndexedImage[2];
		tabID = 3;
		inputTaken = false;
		songChanging = true;
		anIntArray1229 = new int[151];
		aClass11Array1230 = new TileSetting[4];
		aBoolean1233 = false;
		anIntArray1240 = new int[100];
		anIntArray1241 = new int[50];
		aBoolean1242 = false;
		anIntArray1250 = new int[50];
		rsAlreadyLoaded = false;
		welcomeScreenRaised = false;
		promptRaised = false;
		backDialogID = -1;
		anInt1279 = 2;
		bigX = new int[4000];
		bigY = new int[4000];
		anInt1289 = -1;
		publicChatMode = 0;
		privateChatMode = 0;
		clanChatMode = 0;
		yellChatMode = 0;
		tradeMode = 0;
	}

	public RSImage[] background;
	public RSImage button;
	public RSImage button_hover;
	public RSImage field;
	public RSImage field_hover;
	private int ignoreCount;
	private long aLong824;
	private int[][] anIntArrayArray825;
	private int[] friendsNodeIDs;
	private Deque[][][] groundArray;
	private int[] anIntArray828;
	private int[] anIntArray829;
	private volatile boolean aBoolean831;
	private Socket aSocket832;
	private int loginScreenState;
	private ByteBuffer aStream_834;
	private NPC[] npcArray;
	private int npcCount;
	private int[] npcIndices;
	private int anInt839;
	private int[] anIntArray840;
	private int anInt841;
	private int anInt842;
	private int anInt843;
	private String aString844;
	private int privateChatMode;
	private ByteBuffer aStream_847;
	private boolean aBoolean848;
	private static int anInt849;
	private int[] anIntArray850;
	private int[] anIntArray851;
	private int[] anIntArray852;
	private int[] anIntArray853;
	private static int anInt854;
	private int anInt855;
	static int openInterfaceID;
	private int xCameraPos;
	private int zCameraPos;
	private int yCameraPos;
	private int yCameraCurve;
	private int xCameraCurve;
	private int myPrivilege;
	private final int[] currentExp;
	private IndexedImage redStone1_3;
	private IndexedImage redStone2_3;
	private IndexedImage redStone3_2;
	private IndexedImage redStone1_4;
	private IndexedImage redStone2_4;
	private RSImage mapFlag;
	private RSImage mapMarker;
	private boolean aBoolean872;
	private final int[] anIntArray873;
	private int anInt874;
	private final boolean[] aBooleanArray876;
	private int weight;
	private MouseDetection mouseDetection;
	private volatile boolean drawFlames;
	private String reportAbuseInput;
	private int unknownInt10;
	private boolean menuOpen;
	private int anInt886;
	private String inputString;
	private final int maxPlayers;
	private final int myPlayerIndex;
	private Player[] playerArray;
	private int playerCount;
	private int[] playerIndices;
	private int anInt893;
	private int[] anIntArray894;
	private ByteBuffer[] aStreamArray895s;
	private int anInt896;
	private int anInt897;
	private int friendsCount;
	private int anInt900;
	private int[][] anIntArrayArray901;
	private final int anInt902;
	private RSImageProducer backLeftIP1;
	private RSImageProducer backLeftIP2;
	private RSImageProducer backRightIP1;
	private RSImageProducer backRightIP2;
	private RSImageProducer backTopIP1;
	private RSImageProducer backVmidIP1;
	private RSImageProducer backVmidIP2;
	private RSImageProducer backVmidIP3;
	private RSImageProducer backVmidIP2_2;
	private byte[] texturePixels;
	private int anInt913;
	private int crossX;
	private int crossY;
	private int crossIndex;
	private int crossType;
	private int floor_level;
	private final int[] currentStats;
	private static int anInt924;
	private final long[] ignoreListAsLongs;
	private boolean loadingError;
	private final int anInt927;
	private final int[] anIntArray928;
	private int[][] anIntArrayArray929;
	private RSImage aClass30_Sub2_Sub1_Sub1_931;
	private RSImage aClass30_Sub2_Sub1_Sub1_932;
	private int anInt933;
	private int anInt934;
	private int anInt935;
	private int anInt936;
	private int anInt937;
	private int anInt938;
	private static int anInt940;
	private final int[] chatTypes;
	private final String[] chatNames;
	private final String[] chatMessages;
	private int anInt945;
	private SceneGraph worldController;
	private IndexedImage[] sideIcons;
	private int menuScreenArea;
	private int menuOffsetX;
	private int menuOffsetY;
	private int menuWidth;
	private int menuHeight;
	private long aLong953;
	private boolean aBoolean954;
	private long[] friendsListAsLongs;
	private int currentSong;
	private static int nodeID = 10;
	public static int portOff;
	private static boolean isMembers = true;
	private static boolean lowMem;
	private volatile boolean drawingFlames;
	private int spriteDrawX;
	private int spriteDrawY;
	private final int[] anIntArray965 = {
			0xffff00, 0xff0000, 65280, 65535, 0xff00ff, 0xffffff
	};
	private final int[] anIntArray968;
	private final int[] anIntArray969;
	public final ResourceCache[] jagCache;
	public int variousSettings[];
	private boolean aBoolean972;
	private final int anInt975;
	private final int[] anIntArray976;
	private final int[] anIntArray977;
	private final int[] anIntArray978;
	private final int[] anIntArray979;
	private final int[] anIntArray980;
	private final int[] anIntArray981;
	private final int[] anIntArray982;
	private final String[] aStringArray983;
	private int anInt984;
	private int anInt985;
	private static int anInt986;
	private RSImage[] hitMarks;
	private int anInt988;
	private int anInt989;
	private final int[] anIntArray990;
	private static boolean aBoolean993;
	private final boolean aBoolean994;
	private int anInt995;
	private int anInt996;
	private int anInt997;
	private int anInt998;
	private int anInt999;
	private ISAACRandomGen encryption;
	private RSImage mapEdge;
	private final int anInt1002;
	static final int[][] anIntArrayArray1003 = {
		{
			6798, 107, 10283, 16, 4797, 7744, 5799, 4634, 33697, 22433, 
			2983, 54193
		}, {
			8741, 12, 64030, 43162, 7735, 8404, 1701, 38430, 24094, 10153, 
			56621, 4783, 1341, 16578, 35003, 25239
		}, {
			25238, 8742, 12, 64030, 43162, 7735, 8404, 1701, 38430, 24094, 
			10153, 56621, 4783, 1341, 16578, 35003
		}, {
			4626, 11146, 6439, 12, 4758, 10270
		}, {
			4550, 4537, 5681, 5673, 5790, 6806, 8076, 4574
		}
	};
	private String amountOrNameInput;
	private static int anInt1005;
	private int daysSinceLastLogin;
	private int size;
	private int opCode;
	private int anInt1009;
	private int anInt1010;
	private int anInt1011;
	private Deque aClass19_1013;
	private int anInt1014;
	private int anInt1015;
	private int anInt1016;
	private boolean aBoolean1017;
	private int anInt1018;
	private static final int[] anIntArray1019;
	private int anInt1021;
	private int anInt1022;
	private int loadingStage;
	private IndexedImage scrollBar1;
	private IndexedImage scrollBar2;
	private int anInt1026;
	private IndexedImage backBase1;
	private IndexedImage backBase2;
	private IndexedImage backHmid1;
	private final int[] anIntArray1030;
	private boolean aBoolean1031;
	private RSImage[] mapFunctions;
	private int baseX;
	private int baseY;
	private int anInt1036;
	private int anInt1037;
	private int loginFailures;
	private int anInt1039;
	private int anInt1040;
	private int anInt1041;
	private int dialogID;
	private final int[] maxStats;
	private final int[] anIntArray1045;
	private int anInt1046;
	private boolean aBoolean1047;
	private int anInt1048;
	private String aString1049;
	private static int anInt1051;
	private final int[] anIntArray1052;
	private JagexArchive titleArchive;
	private int anInt1054;
	private int anInt1055;
	private Deque aClass19_1056;
	private final int[] anIntArray1057;
	private final RSInterface rsi;
	private IndexedImage[] mapScenes;
	private static int anInt1061;
	private int anInt1062;
	private final int anInt1063;
	private int friendsListAction;
	private final int[] anIntArray1065;
	private int mouseInvInterfaceIndex;
	private int lastActiveInvInterface;
	private ResourceProvider onDemandFetcher;
	private int anInt1069;
	private int anInt1070;
	private int anInt1071;
	private int[] anIntArray1072;
	private int[] anIntArray1073;
	private RSImage mapDotItem;
	private RSImage mapDotNPC;
	private RSImage mapDotPlayer;
	private RSImage mapDotFriend;
	private RSImage mapDotTeam;
	private int anInt1079;
	private boolean aBoolean1080;
	private String[] friendsList;
	private ByteBuffer in;
	private int anInt1084;
	private int anInt1085;
	private int activeInterfaceType;
	private int anInt1087;
	private int anInt1088;
	static int anInt1089;
	private final int[] expectedCRCs;
	private int[] menuActionCmd2;
	private int[] menuActionCmd3;
	private int[] menuActionID;
	private int[] menuActionCmd1;
	private RSImage[] headIcons;
	private static int anInt1097;
	private int anInt1098;
	private int anInt1099;
	private int anInt1100;
	private int anInt1101;
	private int anInt1102;
	private boolean tabAreaAltered;
	private int anInt1104;
	private RSImageProducer aRSImageProducer_1107;
	private RSImageProducer aRSImageProducer_1108;
	private RSImageProducer title;
	private RSImageProducer leftFlames;
	private RSImageProducer rightFlames;
	private RSImageProducer aRSImageProducer_1112;
	private RSImageProducer aRSImageProducer_1113;
	private RSImageProducer aRSImageProducer_1114;
	private RSImageProducer aRSImageProducer_1115;
	private static int lastAction;
	private int membersInt;
	private String promptMessage;
	private RSImage compass;
	private RSImageProducer aRSImageProducer_1123;
	private RSImageProducer aRSImageProducer_1124;
	private RSImageProducer aRSImageProducer_1125;
	public static Player myPlayer;
	private final String[] atPlayerActions;
	private final boolean[] atPlayerArray;
	private final int[][][] anIntArrayArrayArray1129;
	static final int[] tabInterfaceIDs = {
			-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1
	};
	private int anInt1131;
	private int anInt1132;
	private int menuActionRow;
	private static int anInt1134;
	private int spellSelected;
	private int anInt1137;
	private int spellUsableOn;
	private String spellTooltip;
	private RSImage[] aClass30_Sub2_Sub1_Sub1Array1140;
	private boolean aBoolean1141;
	private static int anInt1142;
	private IndexedImage redStone1;
	private IndexedImage redStone2;
	private IndexedImage redStone3;
	private IndexedImage redStone1_2;
	private IndexedImage redStone2_2;
	private int energy;
	private boolean aBoolean1149;
	private RSImage[] crosses;
	private boolean musicEnabled;
	private IndexedImage[] aBackgroundArray1152s;
	private boolean redrawTabArea;
	private int unreadMessages;
	private static int anInt1155;
	private static boolean fpsOn;
	public boolean loggedIn;
	private boolean canMute;
	private boolean aBoolean1159;
	private boolean aBoolean1160;
	public static int currentTime;
	private static final String validUserPassChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!\"\243$%^&*()-_=+[{]};:'@#~,<.>/?\\| ";
	private RSImageProducer tabArea;
	private RSImageProducer mapArea;
	private RSImageProducer gameArea;
	private RSImageProducer chatArea;
	private int daysSinceRecovChange;
	private RSSocket socketStream;
	private int anInt1169;
	private int minimapInt3;
	private int anInt1171;
	private long aLong1172;
	private String myUsername;
	private String myPassword;
	private static int anInt1175;
	private boolean genericLoadingError;
	private final int[] anIntArray1177 = {
			0, 0, 0, 0, 1, 1, 1, 1, 1, 2, 
			2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 
			2, 2, 3
	};
	private int reportAbuseInterfaceID;
	private Deque deque;
	private int[] chatAreaTextureArray;
	private int[] tabAreaTextureArray;
	private int[] gameAreaTextureArray;
	private byte[][] aByteArrayArray1183;
	private int anInt1184;
	private int minimapInt1;
	private int anInt1186;
	private int anInt1187;
	private static int anInt1188;
	private int invOverlayInterfaceID;
	private int[] anIntArray1190;
	private int[] anIntArray1191;
	private ByteBuffer out;
	private int anInt1193;
	private int splitPrivateChat;
	private IndexedImage invBack;
	private IndexedImage mapBack;
	private IndexedImage chatBack;
	private String[] menuActionName;
	private RSImage aClass30_Sub2_Sub1_Sub1_1201;
	private RSImage aClass30_Sub2_Sub1_Sub1_1202;
	private final int[] anIntArray1203;
	static final int[] anIntArray1204 = {
		9104, 10275, 7595, 3610, 7975, 8526, 918, 38802, 24466, 10145, 
		58654, 5027, 1457, 16565, 34991, 25486
	};
	private static boolean flagged;
	private final int[] anIntArray1207;
	private int anInt1208;
	private int minimapInt2;
	private int anInt1210;
	public static int anInt1211;
	private String promptInput;
	private int anInt1213;
	private int[][][] intGroundArray;
	private long aLong1215;
	private int loginCursorPos;
	private final IndexedImage[] modIcons;
	private long aLong1220;
	static int tabID;
	private int anInt1222;
	static boolean inputTaken;
	static int dialogState;
	private static int anInt1226;
	private int nextSong;
	private boolean songChanging;
	private final int[] anIntArray1229;
	private TileSetting[] aClass11Array1230;
	public static int anIntArray1232[];
	private boolean aBoolean1233;
	private int[] anIntArray1234;
	private int[] anIntArray1235;
	private int[] anIntArray1236;
	private int anInt1237;
	private int anInt1238;
	public final int anInt1239 = 100;
	private final int[] anIntArray1240;
	private final int[] anIntArray1241;
	private boolean aBoolean1242;
	private int atInventoryLoopCycle;
	private int atInventoryInterface;
	private int atInventoryIndex;
	private int atInventoryInterfaceType;
	private byte[][] aByteArrayArray1247;
	private int tradeMode;
	public int clanChatMode;
	public int yellChatMode;
	private int anInt1249;
	private final int[] anIntArray1250;
	private int anInt1251;
	private final boolean rsAlreadyLoaded;
	private int anInt1253;
	private int anInt1254;
	private boolean welcomeScreenRaised;
	private boolean promptRaised;
	private int anInt1257;
	private byte[][][] byteGroundArray;
	private int prevSong;
	private int destX;
	private int destY;
	private RSImage aClass30_Sub2_Sub1_Sub1_1263;
	private int anInt1264;
	private int anInt1265;
	private String loginMessage1;
	private String loginMessage2;
	private int anInt1268;
	private int anInt1269;
	public RSFont small;
	public RSFont regular;
	public RSFont bold;
	public RSFont fancy;
	private int anInt1275;
	int backDialogID;
	private int anInt1278;
	private int anInt1279;
	private int[] bigX;
	private int[] bigY;
	private int itemSelected;
	private int anInt1283;
	private int anInt1284;
	private int anInt1285;
	private String selectedItemName;
	private int publicChatMode;
	private static int anInt1288;
	private int anInt1289;
	public static int anInt1290;public int drawCount;

    public int fullscreenInterfaceID;
    public int anInt1044;//377
    public int anInt1129;//377
    public int anInt1315;//377
    public int anInt1500;//377
    public int anInt1501;//377
    public int[] fullScreenTextureArray;

	public void resetAllImageProducers() {
        if (super.fullGameScreen != null) {
            return;
        }
        tabArea = null;
        mapArea = null;
        chatArea = null;
        gameArea = null;
        title = null;
        aRSImageProducer_1123 = null;
        aRSImageProducer_1124 = null;
        aRSImageProducer_1125 = null;
        aRSImageProducer_1107 = null;
        aRSImageProducer_1108 = null;
        aRSImageProducer_1112 = null;
        aRSImageProducer_1113 = null;
        aRSImageProducer_1114 = null;
        aRSImageProducer_1115 = null;
        super.fullGameScreen = new RSImageProducer(getClientWidth(), getClientHeight(), getGameComponent());
        welcomeScreenRaised = true;

    }

	public static int clientSize = 0;
	public static int REGULAR_WIDTH = 765, REGULAR_HEIGHT = 503,
			RESIZABLE_WIDTH = 800, RESIZABLE_HEIGHT = 600;
	public static int clientWidth = REGULAR_WIDTH;
	public int clientHeight = REGULAR_HEIGHT;
	
	public static boolean isFullScreen;
	public static boolean isResizable;

	public void setResizable(boolean resizable) {
		isResizable = resizable;
	}

	public boolean isResizable() {
		return isResizable;
	}

	public void setFullScreen(boolean fullscreen) {
		isFullScreen = fullscreen;
	}

	public boolean isFullScreen() {
		return isFullScreen;
	}

	public boolean isFixed() {
		return !isResizable() && !isFullScreen();
	}

	private static Toolkit toolkit;
	private static Dimension screenSize;
	private int gameAreaWidth = 512, gameAreaHeight = 334;
	
	public void recreateClientFrame(boolean undecorative, int width, int height, boolean resizable, int displayMode) {
		gameAreaWidth = (displayMode == 0) ? 512 : width;
		gameAreaHeight = (displayMode == 0) ? 334 : height;
		clientWidth = width;
		clientHeight = height;
		getClient().recreateClientFrame(undecorative, width, height, resizable);
		updateGameArea();
		super.mouseX = super.mouseY = -1;
	}

	private void updateGameArea() {
		Rasterizer.setBounds(getClientWidth(), getClientHeight());
        fullScreenTextureArray = Rasterizer.lineOffsets;
		Rasterizer.setBounds(isFixed() ? 519 : getClientWidth(), isFixed() ? 165 : getClientHeight());
		chatAreaTextureArray = Rasterizer.lineOffsets;
		Rasterizer.setBounds(isFixed() ? 246 : getClientWidth(), isFixed() ? 335 : getClientHeight());
		tabAreaTextureArray = Rasterizer.lineOffsets;
		Rasterizer.setBounds(gameAreaWidth, gameAreaHeight);
		gameAreaTextureArray = Rasterizer.lineOffsets;
		int ai[] = new int[9];
		for(int index = 0; index < 9; index++) {
			int k8 = 128 + index * 32 + 15;
			int l8 = 600 + k8 * 3;
			int i9 = Rasterizer.SINE[k8];
			ai[index] = l8 * i9 >> 16;
		}
		SceneGraph.setupViewport(500, 800, gameAreaWidth, gameAreaHeight, ai);
		gameArea = new RSImageProducer(gameAreaWidth, gameAreaHeight, getGameComponent());
	}

	public void setClientDimensions(int width, int height) {
		gameAreaWidth = width;
		gameAreaHeight = height;
	}
	
	public int getClientWidth() {
		int width = 0;
		if(isFullScreen()) {
			toolkit = Toolkit.getDefaultToolkit();
			screenSize = toolkit.getScreenSize();
			width = (int) screenSize.getWidth();
		} else if (isResizable()) {
			width = mainFrame.getFrameWidth();
		} else {
			width = 765;
		}
		return width;
	}
	
	public int getClientHeight() {
		int height = 0;
		if(isFullScreen()) {
			toolkit = Toolkit.getDefaultToolkit();
			screenSize = toolkit.getScreenSize();
			height = (int) screenSize.getHeight();
		} else if (isResizable()) {
			height = mainFrame.getFrameHeight();
		} else {
			height = 503;
		}
		return height;
	}

	public void toggleSize(int size) {
		if (clientSize != size) {
			clientSize = size;
			setFullScreen(clientSize > 1);
			setResizable(clientSize == 1);
			switch (clientSize) {
				case 0:
					recreateClientFrame(false, REGULAR_WIDTH, REGULAR_HEIGHT, false, clientSize);
					break;
				case 1:
					recreateClientFrame(false, RESIZABLE_WIDTH, RESIZABLE_HEIGHT, true, clientSize);
					break;
				case 2:
					recreateClientFrame(true, getClientWidth(), getClientHeight(), false, clientSize);
					break;
			}
		}
	}

	public int getResizableWidth() {
		return mainFrame.getFrameWidth();
	}

	public int getResizableHeight() {
		return mainFrame.getFrameHeight();
	}

	public int getGameAreaX() {
		return isFixed() ? 4 : 0;
	}

	public int getGameAreaY() {
		return isFixed() ? 4 : 0;
	}

	static 
	{
		anIntArray1019 = new int[99];
		int i = 0;
		for(int j = 0; j < 99; j++)
		{
			int l = j + 1;
			int i1 = (int)((double)l + 300D * Math.pow(2D, (double)l / 7D));
			i += i1;
			anIntArray1019[j] = i / 4;
		}

		anIntArray1232 = new int[32];
		i = 2;
		for(int k = 0; k < 32; k++)
		{
			anIntArray1232[k] = i - 1;
			i += i;
		}

	}
}
