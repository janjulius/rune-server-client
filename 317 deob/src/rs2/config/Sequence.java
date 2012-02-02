package rs2.config;

import rs2.Class36;
import rs2.ByteBuffer;
import rs2.cache.JagexArchive;

public final class Sequence {

	public static void unpackConfig(JagexArchive streamLoader) {
		ByteBuffer stream = new ByteBuffer(streamLoader.getData("seq.dat"));
		total = stream.getShort();
		if (anims == null)
			anims = new Sequence[total];
		for (int j = 0; j < total; j++) {
			if (anims[j] == null)
				anims[j] = new Sequence();
			anims[j].readValues(stream);
		}
	}

	/**
	 * Returns the sequence for a given id.
	 * @param id
	 * @return
	 */
	public static Sequence getSeq(int id) {
		if (id > total - 1) {
			return anims[808];
		}
		if (anims[id] == null) {
			return anims[808];
		}
		return anims[id];
	}

	public int method258(int i) {
		int j = anIntArray355[i];
		if (j == 0) {
			Class36 class36 = Class36.method531(anIntArray353[i]);
			if (class36 != null)
				j = anIntArray355[i] = class36.anInt636;
		}
		if (j == 0)
			j = 1;
		return j;
	}

	private void readValues(ByteBuffer stream) {
		do {
			int i = stream.getUByte();
			if (i == 0)
				break;
			if (i == 1) {
				anInt352 = stream.getUByte();
				anIntArray353 = new int[anInt352];
				anIntArray354 = new int[anInt352];
				anIntArray355 = new int[anInt352];
				for (int j = 0; j < anInt352; j++) {
					anIntArray353[j] = stream.getShort();
					anIntArray354[j] = stream.getShort();
					if (anIntArray354[j] == 65535)
						anIntArray354[j] = -1;
					anIntArray355[j] = stream.getShort();
				}

			} else if (i == 2)
				anInt356 = stream.getShort();
			else if (i == 3) {
				int k = stream.getUByte();
				anIntArray357 = new int[k + 1];
				for (int l = 0; l < k; l++)
					anIntArray357[l] = stream.getUByte();

				anIntArray357[k] = 0x98967f;
			} else if (i == 4)
				aBoolean358 = true;
			else if (i == 5)
				anInt359 = stream.getUByte();
			else if (i == 6)
				anInt360 = stream.getShort();
			else if (i == 7)
				anInt361 = stream.getShort();
			else if (i == 8)
				anInt362 = stream.getUByte();
			else if (i == 9)
				anInt363 = stream.getUByte();
			else if (i == 10)
				anInt364 = stream.getUByte();
			else if (i == 11)
				anInt365 = stream.getUByte();
			else if (i == 12)
				stream.getInt();
			else
				System.out.println("Error unrecognised seq config code: " + i);
		} while (true);
		if (anInt352 == 0) {
			anInt352 = 1;
			anIntArray353 = new int[1];
			anIntArray353[0] = -1;
			anIntArray354 = new int[1];
			anIntArray354[0] = -1;
			anIntArray355 = new int[1];
			anIntArray355[0] = -1;
		}
		if (anInt363 == -1)
			if (anIntArray357 != null)
				anInt363 = 2;
			else
				anInt363 = 0;
		if (anInt364 == -1) {
			if (anIntArray357 != null) {
				anInt364 = 2;
				return;
			}
			anInt364 = 0;
		}
	}

	private Sequence() {
		anInt356 = -1;
		aBoolean358 = false;
		anInt359 = 5;
		anInt360 = -1;
		anInt361 = -1;
		anInt362 = 99;
		anInt363 = -1;
		anInt364 = -1;
		anInt365 = 2;
	}

	public static int total;
	public static Sequence anims[];
	public int anInt352;
	public int anIntArray353[];
	public int anIntArray354[];
	private int[] anIntArray355;
	public int anInt356;
	public int anIntArray357[];
	public boolean aBoolean358;
	public int anInt359;
	public int anInt360;
	public int anInt361;
	public int anInt362;
	public int anInt363;
	public int anInt364;
	public int anInt365;
	public static int anInt367;
}