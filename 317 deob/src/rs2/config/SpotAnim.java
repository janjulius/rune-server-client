package rs2.config;

import rs2.MRUNodes;
import rs2.Model;
import rs2.ByteBuffer;
import rs2.cache.JagexArchive;

public final class SpotAnim {

	public static void unpackConfig(JagexArchive streamLoader) {
		ByteBuffer stream = new ByteBuffer(streamLoader.getData("spotanim.dat"));
		int length = stream.getShort();
		if (cache == null)
			cache = new SpotAnim[length];
		for (int j = 0; j < length; j++) {
			if (cache[j] == null)
				cache[j] = new SpotAnim();
			cache[j].anInt404 = j;
			cache[j].readValues(stream);
		}

	}

	private void readValues(ByteBuffer stream) {
		do {
			int i = stream.getUByte();
			if (i == 0)
				return;
			if (i == 1)
				anInt405 = stream.getShort();
			else if (i == 2) {
				anInt406 = stream.getShort();
				if (Sequence.anims != null)
					aAnimation_407 = Sequence.getSeq(anInt406);
			} else if (i == 4)
				anInt410 = stream.getShort();
			else if (i == 5)
				anInt411 = stream.getShort();
			else if (i == 6)
				anInt412 = stream.getShort();
			else if (i == 7)
				anInt413 = stream.getUByte();
			else if (i == 8)
				anInt414 = stream.getUByte();
			else if (i >= 40 && i < 50)
				anIntArray408[i - 40] = stream.getShort();
			else if (i >= 50 && i < 60)
				anIntArray409[i - 50] = stream.getShort();
			else
				System.out.println("Error unrecognised spotanim config code: "
						+ i);
		} while (true);
	}

	public Model getModel() {
		Model model = (Model) aMRUNodes_415.insertFromCache(anInt404);
		if (model != null)
			return model;
		model = Model.method462(anInt405);
		if (model == null)
			return null;
		for (int i = 0; i < 6; i++)
			if (anIntArray408[0] != 0)
				model.changeModelColors(anIntArray408[i], anIntArray409[i]);

		aMRUNodes_415.removeFromCache(model, anInt404);
		return model;
	}

	private SpotAnim() {
		anInt406 = -1;
		anIntArray408 = new int[6];
		anIntArray409 = new int[6];
		anInt410 = 128;
		anInt411 = 128;
	}

	public static SpotAnim cache[];
	private int anInt404;
	private int anInt405;
	private int anInt406;
	public Sequence aAnimation_407;
	private final int[] anIntArray408;
	private final int[] anIntArray409;
	public int anInt410;
	public int anInt411;
	public int anInt412;
	public int anInt413;
	public int anInt414;
	public static MRUNodes aMRUNodes_415 = new MRUNodes(30);

}