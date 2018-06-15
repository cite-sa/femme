package gr.cite.commons.utils.hash;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashGeneratorUtils {
	public static String generateMD5(String value) throws HashGenerationException {
		return hash(value, "MD5");

	}

	private static String hash(String value, String hashAlgorithm) throws HashGenerationException {
		try (InputStream inputStream = new ByteArrayInputStream(value.getBytes())) {
			MessageDigest digest = MessageDigest.getInstance(hashAlgorithm);

			byte[] bytesBuffer = new byte[1024];
			int bytesRead = -1;

			while ((bytesRead = inputStream.read(bytesBuffer)) != -1) {
				digest.update(bytesBuffer, 0, bytesRead);
			}

			byte[] hashedBytes = digest.digest();

			return convertByteArrayToHexString(hashedBytes);
		} catch (NoSuchAlgorithmException | IOException ex) {
			throw new HashGenerationException("Could not generate getChecksum from string", ex);
		}

	}

	private static String convertByteArrayToHexString(byte[] bytes) {
		StringBuffer sb = new StringBuffer("");
		for (int i = 0; i < bytes.length; i++) {
			sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
		}
		return sb.toString();
	}
	
}
