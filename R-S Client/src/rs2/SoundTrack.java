package rs2; 

final class SoundTrack {

	public static void initialize() {
		noise = new int[32768];
		for (int i = 0; i < 32768; i++)
			if (Math.random() > 0.5D)
				noise[i] = 1;
			else
				noise[i] = -1;
		sineTable = new int[32768];
		for (int j = 0; j < 32768; j++)
			sineTable[j] = (int) (Math.sin((double) j / 5215.1903000000002D) * 16384D);
		sampleBuffer = new int[0x35d54];
	}

	public int[] buildSoundData(int sampleLength, int j) {
		for (int k = 0; k < sampleLength; k++)
			sampleBuffer[k] = 0;

		if (j < 10)
			return sampleBuffer;
		double d = (double) sampleLength / ((double) j + 0.0D);
		sample1.resetValues();
		sample2.resetValues();
		int l = 0;
		int i1 = 0;
		int j1 = 0;
		if (sample3 != null) {
			sample3.resetValues();
			sample4.resetValues();
			l = (int) (((double) (sample3.anInt539 - sample3.anInt538) * 32.768000000000001D) / d);
			i1 = (int) (((double) sample3.anInt538 * 32.768000000000001D) / d);
		}
		int k1 = 0;
		int l1 = 0;
		int i2 = 0;
		if (sample5 != null) {
			sample5.resetValues();
			sample6.resetValues();
			k1 = (int) (((double) (sample5.anInt539 - sample5.anInt538) * 32.768000000000001D) / d);
			l1 = (int) (((double) sample5.anInt538 * 32.768000000000001D) / d);
		}
		for (int j2 = 0; j2 < 5; j2++)
			if (anIntArray106[j2] != 0) {
				phase[j2] = 0;
				anIntArray119[j2] = (int) ((double) anIntArray108[j2] * d);
				anIntArray120[j2] = (anIntArray106[j2] << 14) / 100;
				anIntArray121[j2] = (int) (((double) (sample1.anInt539 - sample1.anInt538) * 32.768000000000001D * Math.pow(1.0057929410678534D, anIntArray107[j2])) / d);
				anIntArray122[j2] = (int) (((double) sample1.anInt538 * 32.768000000000001D) / d);
			}

		for (int k2 = 0; k2 < sampleLength; k2++) {
			int l2 = sample1.currentAmplitude(sampleLength);
			int j4 = sample2.currentAmplitude(sampleLength);
			if (sample3 != null) {
				int j5 = sample3.currentAmplitude(sampleLength);
				int j6 = sample4.currentAmplitude(sampleLength);
				l2 += getValue(j6, j1, sample3.form) >> 1;
				j1 += (j5 * l >> 16) + i1;
			}
			if (sample5 != null) {
				int k5 = sample5.currentAmplitude(sampleLength);
				int k6 = sample6.currentAmplitude(sampleLength);
				j4 = j4* ((getValue(k6, i2, sample5.form) >> 1) + 32768) >> 15;
				i2 += (k5 * k1 >> 16) + l1;
			}
			for (int l5 = 0; l5 < 5; l5++)
				if (anIntArray106[l5] != 0) {
					int l6 = k2 + anIntArray119[l5];
					if (l6 < sampleLength) {
						sampleBuffer[l6] += getValue(j4 * anIntArray120[l5] >> 15, phase[l5], sample1.form);
						phase[l5] += (l2 * anIntArray121[l5] >> 16) + anIntArray122[l5];
					}
				}

		}

		if (aClass29_104 != null) {
			aClass29_104.resetValues();
			aClass29_105.resetValues();
			int i3 = 0;
			boolean flag1 = true;
			for (int i7 = 0; i7 < sampleLength; i7++) {
				int k7 = aClass29_104.currentAmplitude(sampleLength);
				int i8 = aClass29_105.currentAmplitude(sampleLength);
				int k4;
				if (flag1)
					k4 = aClass29_104.anInt538 + ((aClass29_104.anInt539 - aClass29_104.anInt538) * k7 >> 8);
				else
					k4 = aClass29_104.anInt538 + ((aClass29_104.anInt539 - aClass29_104.anInt538) * i8 >> 8);
				if ((i3 += 256) >= k4) {
					i3 = 0;
					flag1 = !flag1;
				}
				if (flag1)
					sampleBuffer[i7] = 0;
			}

		}
		if (anInt109 > 0 && gain > 0) {
			int j3 = (int) ((double) anInt109 * d);
			for (int l4 = j3; l4 < sampleLength; l4++)
				sampleBuffer[l4] += (sampleBuffer[l4 - j3] * gain) / 100;

		}
		if (aClass39_111.anIntArray665[0] > 0 || aClass39_111.anIntArray665[1] > 0) {
			aClass29_112.resetValues();
			int k3 = aClass29_112.currentAmplitude(sampleLength + 1);
			int i5 = aClass39_111.method544(0, (float) k3 / 65536F);
			int i6 = aClass39_111.method544(1, (float) k3 / 65536F);
			if (sampleLength >= i5 + i6) {
				int j7 = 0;
				int l7 = i6;
				if (l7 > sampleLength - i5)
					l7 = sampleLength - i5;
				for (; j7 < l7; j7++) {
					int j8 = (int) ((long) sampleBuffer[j7 + i5] * (long) FrequencyGenerator.anInt672 >> 16);
					for (int k8 = 0; k8 < i5; k8++)
						j8 += (int) ((long) sampleBuffer[(j7 + i5) - 1 - k8] * (long) FrequencyGenerator.anIntArrayArray670[0][k8] >> 16);

					for (int j9 = 0; j9 < j7; j9++)
						j8 -= (int) ((long) sampleBuffer[j7 - 1 - j9] * (long) FrequencyGenerator.anIntArrayArray670[1][j9] >> 16);

					sampleBuffer[j7] = j8;
					k3 = aClass29_112.currentAmplitude(sampleLength + 1);
				}

				char c = '\200';
				l7 = c;
				do {
					if (l7 > sampleLength - i5)
						l7 = sampleLength - i5;
					for (; j7 < l7; j7++) {
						int l8 = (int) ((long) sampleBuffer[j7 + i5] * (long) FrequencyGenerator.anInt672 >> 16);
						for (int k9 = 0; k9 < i5; k9++)
							l8 += (int) ((long) sampleBuffer[(j7 + i5) - 1 - k9] * (long) FrequencyGenerator.anIntArrayArray670[0][k9] >> 16);

						for (int i10 = 0; i10 < i6; i10++)
							l8 -= (int) ((long) sampleBuffer[j7 - 1 - i10] * (long) FrequencyGenerator.anIntArrayArray670[1][i10] >> 16);

						sampleBuffer[j7] = l8;
						k3 = aClass29_112.currentAmplitude(sampleLength + 1);
					}

					if (j7 >= sampleLength - i5)
						break;
					i5 = aClass39_111.method544(0, (float) k3 / 65536F);
					i6 = aClass39_111.method544(1, (float) k3 / 65536F);
					l7 += c;
				} while (true);
				for (; j7 < sampleLength; j7++) {
					int i9 = 0;
					for (int l9 = (j7 + i5) - sampleLength; l9 < i5; l9++)
						i9 += (int) ((long) sampleBuffer[(j7 + i5) - 1 - l9] * (long) FrequencyGenerator.anIntArrayArray670[0][l9] >> 16);

					for (int j10 = 0; j10 < i6; j10++)
						i9 -= (int) ((long) sampleBuffer[j7 - 1 - j10] * (long) FrequencyGenerator.anIntArrayArray670[1][j10] >> 16);

					sampleBuffer[j7] = i9;
					aClass29_112.currentAmplitude(sampleLength + 1);
				}

			}
		}
		for (int i4 = 0; i4 < sampleLength; i4++) {
			if (sampleBuffer[i4] < -32768)
				sampleBuffer[i4] = -32768;
			if (sampleBuffer[i4] > 32767)
				sampleBuffer[i4] = 32767;
		}

		return sampleBuffer;
	}

	private int getValue(int volume, int phase, int envelopeType) {
		if (envelopeType == 1)
			if ((phase & 0x7fff) < 16384)
				return volume;
			else
				return -volume;
		if (envelopeType == 2)
			return sineTable[phase & 0x7fff] * volume >> 14;
		if (envelopeType == 3)
			return ((phase & 0x7fff) * volume >> 14) - volume;
		if (envelopeType == 4)
			return noise[phase / 2607 & 0x7fff] * volume;
		else
			return 0;
	}

	public void unpack(ByteBuffer buffer) {
		sample1 = new AmplitudeEnvelope();
		sample1.method325(buffer);
		sample2 = new AmplitudeEnvelope();
		sample2.method325(buffer);
		int i = buffer.getUByte();
		if (i != 0) {
			buffer.offset--;
			sample3 = new AmplitudeEnvelope();
			sample3.method325(buffer);
			sample4 = new AmplitudeEnvelope();
			sample4.method325(buffer);
		}
		i = buffer.getUByte();
		if (i != 0) {
			buffer.offset--;
			sample5 = new AmplitudeEnvelope();
			sample5.method325(buffer);
			sample6 = new AmplitudeEnvelope();
			sample6.method325(buffer);
		}
		i = buffer.getUByte();
		if (i != 0) {
			buffer.offset--;
			aClass29_104 = new AmplitudeEnvelope();
			aClass29_104.method325(buffer);
			aClass29_105 = new AmplitudeEnvelope();
			aClass29_105.method325(buffer);
		}
		for (int j = 0; j < 10; j++) {
			int k = buffer.method422();
			if (k == 0)
				break;
			anIntArray106[j] = k;
			anIntArray107[j] = buffer.method421();
			anIntArray108[j] = buffer.method422();
		}

		anInt109 = buffer.method422();
		gain = buffer.method422();
		msLength = buffer.getShort();
		anInt114 = buffer.getShort();
		aClass39_111 = new FrequencyGenerator();
		aClass29_112 = new AmplitudeEnvelope();
		aClass39_111.method545(buffer, aClass29_112);
	}

	public SoundTrack() {
		anIntArray106 = new int[5];
		anIntArray107 = new int[5];
		anIntArray108 = new int[5];
		gain = 100;
		msLength = 500;
	}

	private AmplitudeEnvelope sample1;
	private AmplitudeEnvelope sample2;
	private AmplitudeEnvelope sample3;
	private AmplitudeEnvelope sample4;
	private AmplitudeEnvelope sample5;
	private AmplitudeEnvelope sample6;
	private AmplitudeEnvelope aClass29_104;
	private AmplitudeEnvelope aClass29_105;
	private final int[] anIntArray106;
	private final int[] anIntArray107;
	private final int[] anIntArray108;
	private int anInt109;
	private int gain;
	private FrequencyGenerator aClass39_111;
	private AmplitudeEnvelope aClass29_112;
	int msLength;
	int anInt114;
	private static int[] sampleBuffer;
	private static int[] noise;
	private static int[] sineTable;
	private static final int[] phase = new int[5];
	private static final int[] anIntArray119 = new int[5];
	private static final int[] anIntArray120 = new int[5];
	private static final int[] anIntArray121 = new int[5];
	private static final int[] anIntArray122 = new int[5];

}
