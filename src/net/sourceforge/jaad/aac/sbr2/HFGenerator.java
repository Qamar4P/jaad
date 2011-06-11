/*
 * Copyright (C) 2010 in-somnia
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.sourceforge.jaad.aac.sbr2;

import net.sourceforge.jaad.aac.AACException;

//TODO: patch and chirp factor calculation are ok, next inverse filtering (p229)
class HFGenerator implements SBRConstants {

	private static final float GOAL_SB_FACTOR = 2.048e6f;
	private static final float RELAX_COEF = 1.000001f;
	private static final float[][] CHIRP_COEFS = {{0.75f, 0.25f}, {0.90625f, 0.09375f}};
	//value for bw [invfModePrev][invfMode]
	private static final float[][] BW_COEFS = {
		{0.0f, 0.6f, 0.9f, 0.98f},
		{0.6f, 0.75f, 0.9f, 0.98f},
		{0.0f, 0.75f, 0.9f, 0.98f},
		{0.0f, 0.75f, 0.9f, 0.98f}
	};
	private static final float CHIRP_MIN = 0.015625f;

	//calculates patch subbands and calls FrequencyTables.calculateLimiterTable
	public static void constructPatches(SBRHeader header, FrequencyTables tables, int sampleRate) throws AACException {
		//get parameters
		final int k0 = tables.getK0();
		final int kx = tables.getKx(false);
		final int m = tables.getM();
		final int[] mft = tables.getMFT();
		final int nMaster = tables.getNMaster();

		//patch construction (flowchart 4.46, p231)
		int msb = k0;
		int usb = kx;
		int patchCount = 0;

		int goalSb = Math.round(GOAL_SB_FACTOR/sampleRate); //TODO: replace with table
		int k;
		if(goalSb<kx+m) {
			k = 0;
			for(int i = 0; mft[i]<goalSb; i++) {
				k = i+1;
			}
		}
		else k = nMaster;

		int[] patchSubbands = new int[0]; //TODO: length
		int[] patchStartSubband = new int[0]; //TODO: length
		int sb, j, odd;
		do {
			j = k+1;
			do {
				j--;
				sb = mft[j];
				odd = (sb-2+k0)%2;
			}
			while(sb>(k0-1+msb-odd));

			patchSubbands[patchCount] = Math.max(sb-usb, 0);
			patchStartSubband[patchCount] = k0-odd-patchSubbands[patchCount];

			if(patchSubbands[patchCount]>0) {
				usb = sb;
				msb = sb;
				patchCount++;
			}
			else msb = kx;

			if(mft[k]-sb<3) k = nMaster;
		}
		while(sb!=(kx+m));

		if(patchSubbands[patchCount-1]<3&&patchCount>1) patchCount--;

		//call function in FrequencyTables to calculate patch borders
		tables.calculateLimiterTable(header, patchCount, patchSubbands, patchStartSubband);
	}

	private static void calculateChirpFactors(FrequencyTables tables, ChannelData cd) {
		//calculates chirp factors and replaces old ones in ChannelData
		final int[] invfMode = cd.getInvfMode(false);
		final int[] invfModePrevious = cd.getInvfMode(true);
		final float[] bwArrayPrevious = cd.getChirpFactors();

		final float[] bwArray = new float[tables.getNq()];
		float[] chirpCoefs;
		for(int i = 0; i<bwArray.length; i++) {
			bwArray[i] = BW_COEFS[invfModePrevious[i]][invfMode[i]];
			chirpCoefs = (bwArray[i]<bwArrayPrevious[i]) ? CHIRP_COEFS[0] : CHIRP_COEFS[1];
			bwArray[i] = (chirpCoefs[0]*bwArray[i])+(chirpCoefs[1]*bwArrayPrevious[i]);
			bwArray[i] = (bwArray[i]<CHIRP_MIN) ? 0 : bwArray[i];
		}

		cd.setChirpFactors(bwArray);
	}
	
	//x: 32 subbands x 32 samples complex input from analysis filterbank
	private static void calculatePredictionCoefs(FrequencyTables tables, float[][][] x, int k) {
		//calculate covariance matrix
		final float[][][] phi = new float[3][2][2];
		float[] tmp;
		for(int i = 0; i<3; i++) {
			for(int j = 0; j<2; j++) {
				phi[i][j][0] = 0;
				phi[i][j][1] = 0;
				for(int n = 0; n<TIME_SLOTS_RATE+6; n++) {
					//complex multiplication with conjugate
					tmp = x[k][n-i+T_HF_ADJ];
					phi[i][j][0] += (tmp[0]*tmp[0]);
					phi[i][j][1] += (tmp[1]*tmp[1]);
				}
			}
		}

		//calculate prediction coefficients
	}
}