package rs2.graphics;

import rs2.Class36;
import rs2.MRUNodes;
import rs2.Model;
import rs2.ByteBuffer;
import rs2.Client;
import rs2.cache.JagexArchive;
import rs2.config.NPCDef;
import rs2.config.ItemDef;
import rs2.util.TextUtils;

public final class RSInterface {

	public void swapInventoryItems(int i, int j) {
		int k = inv[i];
		inv[i] = inv[j];
		inv[j] = k;
		k = invStackSizes[i];
		invStackSizes[i] = invStackSizes[j];
		invStackSizes[j] = k;
	}

	public static void unpack(JagexArchive streamLoader,
			RSFont textDrawingAreas[], JagexArchive streamLoader_1) {
		aMRUNodes_238 = new MRUNodes(50000);
		ByteBuffer stream = new ByteBuffer(streamLoader.getData("data"));
		int i = -1;
		int j = stream.getShort();
		cache = new RSInterface[j];
		while (stream.offset < stream.buffer.length) {
			int k = stream.getShort();
			if (k == 65535) {
				i = stream.getShort();
				k = stream.getShort();
			}
			RSInterface rsInterface = cache[k] = new RSInterface();
			rsInterface.id = k;
			rsInterface.parentID = i;
			rsInterface.type = stream.getUByte();
			rsInterface.atActionType = stream.getUByte();
			rsInterface.anInt214 = stream.getShort();
			rsInterface.width = stream.getShort();
			rsInterface.height = stream.getShort();
			rsInterface.aByte254 = (byte) stream.getUByte();
			rsInterface.anInt230 = stream.getUByte();
			if (rsInterface.anInt230 != 0)
				rsInterface.anInt230 = (rsInterface.anInt230 - 1 << 8)
						+ stream.getUByte();
			else
				rsInterface.anInt230 = -1;
			int i1 = stream.getUByte();
			if (i1 > 0) {
				rsInterface.anIntArray245 = new int[i1];
				rsInterface.anIntArray212 = new int[i1];
				for (int j1 = 0; j1 < i1; j1++) {
					rsInterface.anIntArray245[j1] = stream.getUByte();
					rsInterface.anIntArray212[j1] = stream.getShort();
				}

			}
			int k1 = stream.getUByte();
			if (k1 > 0) {
				rsInterface.valueIndexArray = new int[k1][];
				for (int l1 = 0; l1 < k1; l1++) {
					int i3 = stream.getShort();
					rsInterface.valueIndexArray[l1] = new int[i3];
					for (int l4 = 0; l4 < i3; l4++)
						rsInterface.valueIndexArray[l1][l4] = stream
								.getShort();

				}

			}
			if (rsInterface.type == 0) {
				rsInterface.scrollMax = stream.getShort();
				rsInterface.aBoolean266 = stream.getUByte() == 1;
				int i2 = stream.getShort();
				rsInterface.children = new int[i2];
				rsInterface.childX = new int[i2];
				rsInterface.childY = new int[i2];
				for (int j3 = 0; j3 < i2; j3++) {
					rsInterface.children[j3] = stream.getShort();
					rsInterface.childX[j3] = stream.getSignedShort();
					rsInterface.childY[j3] = stream.getSignedShort();
				}

			}
			if (rsInterface.type == 1) {
				stream.getShort();
				stream.getUByte();
			}
			if (rsInterface.type == 2) {
				rsInterface.inv = new int[rsInterface.width
						* rsInterface.height];
				rsInterface.invStackSizes = new int[rsInterface.width
						* rsInterface.height];
				rsInterface.aBoolean259 = stream.getUByte() == 1;
				rsInterface.isInventoryInterface = stream.getUByte() == 1;
				rsInterface.usableItemInterface = stream.getUByte() == 1;
				rsInterface.aBoolean235 = stream.getUByte() == 1;
				rsInterface.invSpritePadX = stream.getUByte();
				rsInterface.invSpritePadY = stream.getUByte();
				rsInterface.spritesX = new int[20];
				rsInterface.spritesY = new int[20];
				rsInterface.sprites = new RSImage[20];
				for (int j2 = 0; j2 < 20; j2++) {
					int k3 = stream.getUByte();
					if (k3 == 1) {
						rsInterface.spritesX[j2] = stream.getSignedShort();
						rsInterface.spritesY[j2] = stream.getSignedShort();
						String s1 = stream.getString();
						if (streamLoader_1 != null && s1.length() > 0) {
							int i5 = s1.lastIndexOf(",");
							rsInterface.sprites[j2] = method207(
									Integer.parseInt(s1.substring(i5 + 1)),
									streamLoader_1, s1.substring(0, i5));
						}
					}
				}

				rsInterface.actions = new String[5];
				for (int l3 = 0; l3 < 5; l3++) {
					rsInterface.actions[l3] = stream.getString();
					if (rsInterface.actions[l3].length() == 0)
						rsInterface.actions[l3] = null;
				}

			}
			if (rsInterface.type == 3)
				rsInterface.aBoolean227 = stream.getUByte() == 1;
			if (rsInterface.type == 4 || rsInterface.type == 1) {
				rsInterface.aBoolean223 = stream.getUByte() == 1;
				int k2 = stream.getUByte();
				if (textDrawingAreas != null)
					rsInterface.textDrawingAreas = textDrawingAreas[k2];
				rsInterface.aBoolean268 = stream.getUByte() == 1;
			}
			if (rsInterface.type == 4) {
				rsInterface.message = stream.getString();
				rsInterface.aString228 = stream.getString();
			}
			if (rsInterface.type == 1 || rsInterface.type == 3
					|| rsInterface.type == 4)
				rsInterface.textColor = stream.getInt();
			if (rsInterface.type == 3 || rsInterface.type == 4) {
				rsInterface.anInt219 = stream.getInt();
				rsInterface.anInt216 = stream.getInt();
				rsInterface.anInt239 = stream.getInt();
			}
			if (rsInterface.type == 5) {
				String s = stream.getString();
				if (streamLoader_1 != null && s.length() > 0) {
					int i4 = s.lastIndexOf(",");
					rsInterface.sprite1 = method207(
							Integer.parseInt(s.substring(i4 + 1)),
							streamLoader_1, s.substring(0, i4));
				}
				s = stream.getString();
				if (streamLoader_1 != null && s.length() > 0) {
					int j4 = s.lastIndexOf(",");
					rsInterface.sprite2 = method207(
							Integer.parseInt(s.substring(j4 + 1)),
							streamLoader_1, s.substring(0, j4));
				}
			}
			if (rsInterface.type == 6) {
				int l = stream.getUByte();
				if (l != 0) {
					rsInterface.anInt233 = 1;
					rsInterface.mediaID = (l - 1 << 8) + stream.getUByte();
				}
				l = stream.getUByte();
				if (l != 0) {
					rsInterface.anInt255 = 1;
					rsInterface.anInt256 = (l - 1 << 8) + stream.getUByte();
				}
				l = stream.getUByte();
				if (l != 0)
					rsInterface.anInt257 = (l - 1 << 8) + stream.getUByte();
				else
					rsInterface.anInt257 = -1;
				l = stream.getUByte();
				if (l != 0)
					rsInterface.anInt258 = (l - 1 << 8)
							+ stream.getUByte();
				else
					rsInterface.anInt258 = -1;
				rsInterface.modelZoom = stream.getShort();
				rsInterface.modelRotation1 = stream.getShort();
				rsInterface.modelRotation2 = stream.getShort();
			}
			if (rsInterface.type == 7) {
				rsInterface.inv = new int[rsInterface.width
						* rsInterface.height];
				rsInterface.invStackSizes = new int[rsInterface.width
						* rsInterface.height];
				rsInterface.aBoolean223 = stream.getUByte() == 1;
				int l2 = stream.getUByte();
				if (textDrawingAreas != null)
					rsInterface.textDrawingAreas = textDrawingAreas[l2];
				rsInterface.aBoolean268 = stream.getUByte() == 1;
				rsInterface.textColor = stream.getInt();
				rsInterface.invSpritePadX = stream.getSignedShort();
				rsInterface.invSpritePadY = stream.getSignedShort();
				rsInterface.isInventoryInterface = stream.getUByte() == 1;
				rsInterface.actions = new String[5];
				for (int k4 = 0; k4 < 5; k4++) {
					rsInterface.actions[k4] = stream.getString();
					if (rsInterface.actions[k4].length() == 0)
						rsInterface.actions[k4] = null;
				}

			}
			if (rsInterface.atActionType == 2 || rsInterface.type == 2) {
				rsInterface.selectedActionName = stream.getString();
				rsInterface.spellName = stream.getString();
				rsInterface.spellUsableOn = stream.getShort();
			}

			if (rsInterface.type == 8)
				rsInterface.message = stream.getString();

			if (rsInterface.atActionType == 1 || rsInterface.atActionType == 4
					|| rsInterface.atActionType == 5
					|| rsInterface.atActionType == 6) {
				rsInterface.tooltip = stream.getString();
				if (rsInterface.tooltip.length() == 0) {
					if (rsInterface.atActionType == 1)
						rsInterface.tooltip = "Ok";
					if (rsInterface.atActionType == 4)
						rsInterface.tooltip = "Select";
					if (rsInterface.atActionType == 5)
						rsInterface.tooltip = "Select";
					if (rsInterface.atActionType == 6)
						rsInterface.tooltip = "Continue";
				}
			}

			// aryan Bot.notifyInterface(rsInterface);
		}
		aMRUNodes_238 = null;
	}

	private Model method206(int i, int j) {
		Model model = (Model) aMRUNodes_264.insertFromCache((i << 16) + j);
		if (model != null)
			return model;
		if (i == 1)
			model = Model.method462(j);
		if (i == 2)
			model = NPCDef.getNPC(j).method160();
		if (i == 3)
			model = Client.myPlayer.method453();
		if (i == 4)
			model = ItemDef.getDef(j).method202(50);
		if (i == 5)
			model = null;
		if (model != null)
			aMRUNodes_264.removeFromCache(model, (i << 16) + j);
		return model;
	}

	private static RSImage method207(int i, JagexArchive streamLoader, String s) {
		long l = (TextUtils.method585(s) << 8) + (long) i;
		RSImage sprite = (RSImage) aMRUNodes_238.insertFromCache(l);
		if (sprite != null)
			return sprite;
		try {
			sprite = new RSImage(streamLoader, s, i);
			aMRUNodes_238.removeFromCache(sprite, l);
		} catch (Exception _ex) {
			return null;
		}
		return sprite;
	}

	public static void method208(boolean flag, Model model) {
		int i = 0;// was parameter
		int j = 5;// was parameter
		if (flag)
			return;
		aMRUNodes_264.unlinkAll();
		if (model != null && j != 4)
			aMRUNodes_264.removeFromCache(model, (j << 16) + i);
	}

	public Model method209(int j, int k, boolean flag) {
		Model model;
		if (flag)
			model = method206(anInt255, anInt256);
		else
			model = method206(anInt233, mediaID);
		if (model == null)
			return null;
		if (k == -1 && j == -1 && model.anIntArray1640 == null)
			return model;
		Model model_1 = new Model(true, Class36.method532(k)
				& Class36.method532(j), false, model);
		if (k != -1 || j != -1)
			model_1.method469();
		if (k != -1)
			model_1.method470(k);
		if (j != -1)
			model_1.method470(j);
		model_1.method479(64, 768, -50, -10, -50, true);
		return model_1;
	}

	public RSInterface() {
	}

	public RSImage sprite1;
	public int anInt208;
	public RSImage sprites[];
	public static RSInterface cache[];
	public int anIntArray212[];
	public int anInt214;
	public int spritesX[];
	public int anInt216;
	public int atActionType;
	public String spellName;
	public int anInt219;
	public int width;
	public String tooltip;
	public String selectedActionName;
	public boolean aBoolean223;
	public int scrollPosition;
	public String actions[];
	public int valueIndexArray[][];
	public boolean aBoolean227;
	public String aString228;
	public int anInt230;
	public int invSpritePadX;
	public int textColor;
	public int anInt233;
	public int mediaID;
	public boolean aBoolean235;
	public int parentID;
	public int spellUsableOn;
	private static MRUNodes aMRUNodes_238;
	public int anInt239;
	public int children[];
	public int childX[];
	public boolean usableItemInterface;
	public RSFont textDrawingAreas;
	public int invSpritePadY;
	public int anIntArray245[];
	public int anInt246;
	public int spritesY[];
	public String message;
	public boolean isInventoryInterface;
	public int id;
	public int invStackSizes[];
	public int inv[];
	public byte aByte254;
	private int anInt255;
	private int anInt256;
	public int anInt257;
	public int anInt258;
	public boolean aBoolean259;
	public RSImage sprite2;
	public int scrollMax;
	public int type;
	public int anInt263;
	private static final MRUNodes aMRUNodes_264 = new MRUNodes(30);
	public int anInt265;
	public boolean aBoolean266;
	public int height;
	public boolean aBoolean268;
	public int modelZoom;
	public int modelRotation1;
	public int modelRotation2;
	public int childY[];

}
