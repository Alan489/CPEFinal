import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.*;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import java.nio.ByteBuffer;

public class PNG {

	// Status flag:
	// Allow main thread to find out the status of the PNG file.
	// An initial check is just that the file starts with the PNG flags, has a IHDR
	// chunk and an IEND chunk.
	// Full validation checks each chunks CRC flag.

	// Status:
	// 3 - Full validation pass.
	// 2 - Full validation in progress, pass initial.
	// 1 - Initial check pass.
	// 0 - Initial check in progress.
	// -1 - Did not pass initial PNG check.
	// -2 - Did not pass full validation.
	private int status = 0;

	// Private variables
	private File file;
	private FileInputStream reader;

	/*
	 * Width: 4 bytes Height: 4 bytes Bit depth: 1 byte Color type: 1 byte
	 * Compression method: 1 byte Filter method: 1 byte Interlace method: 1 byte
	 */

	// VV Public variables VV

	// Header information
	public long width;
	public long height;
	public int bitDepth;
	public int colorType;
	public int compression;
	public int filter;
	public int interlace;
	public BufferedImage output;
	public ColorTriple background = new ColorTriple(0, 0, 0);
	double gamma = 1;

	private boolean usessRGB = false;
	private palate pt;

	// To prevent long to int overflow errors, pixel data is stored in an arraylist.
	private ArrayList<ArrayList<Integer>> px = new ArrayList<>();

	// END public variables.

	// Basic static arrays. Used for quick comparisons.
	private static int[] IPNG = { 137, 80, 78, 71, 13, 10, 26, 10 };

	private static int[] IHDR = { 'I', 'H', 'D', 'R' };

	private static int[] IEND = { 'I', 'E', 'N', 'D' };

	private static int[] PLTE = { 'P', 'L', 'T', 'E' };

	private static int[] sRGB = { 's', 'R', 'G', 'B' };

	private static int[] IDAT = { 'I', 'D', 'A', 'T' };

	private static int[] BKGD = { 'b', 'K', 'G', 'D' };

	private static int[] GAMA = { 'g', 'A', 'M', 'A' };

	// These two tables contain information regarding color type/bit depth
	// information.
	// This file will convert a "color type" int to a lookup value for a table with
	// valid "bit depth" values.
	private static int[] colorTypeLookupTable = { 0, -1, 1, 2, 3, -1, 4 };
	private static int[][] allowedBitDepths = { { 1, 2, 4, 8, 16 }, { 8, 16, 0, 0, 0 }, { 1, 2, 4, 8, 0 },
			{ 8, 16, 0, 0, 0 }, { 8, 16, 0, 0, 0 } };

	// Flag to stop loading chunks.
	private boolean stop = false;

	// Full arraylist of chunks in the file.
	private ArrayList<Chunk> chunks = new ArrayList<>();

	// Get file, do initial check.
	public PNG(File f) throws Exception {

		file = f;
		reader = new FileInputStream(f);

		// Validate PNG and IHDR
		int[] firstEight = getXInts(8);

		// Make sure the file starts with the proper heading for PNG.
		if (!compareIntArrays(IPNG, firstEight)) {
			status = -1;
			return;
		}

		// Get data from all the chunks.
		while (!stop)
			try {

				chunks.add(new Chunk(reader, this));
				if (chunks.get(chunks.size() - 1).complete = false) {
					// Did the chunk successfully load?
					System.out.println("Issue reading chunk " + chunks.size() + ".");
					stop = true;
					status = -1;
					reader.close();
					return;
				}
			} catch (Exception e) {
				// If not, notify error.
				stop = true;
				status = -1;
				reader.close();
				return;
			}

		// Is the first read chunk the header?
		if (chunks.get(0).type == 1229472850) {
			System.out.println("IHDR is first.");

		} else {
			// If not, notify error.
			System.out.println("Fail; IHDR is NOT first chunk.");
			status = -1;
			reader.close();
			return;
		}

		// No memory leaks here!
		reader.close();

		// Chunk loading completed. Begin setting information.

		System.out.println("Chunk loading complete. Details:");

		// Printing out details and getting dimensions, bit depth, color mode,
		// compression method, filter method and interlace method.
		System.out.println("File contains " + chunks.size() + " chunks.");
		status = 1;

		Chunk header = chunks.get(0);

		// Calculate width and height; 4byte integer.
		// Again, Java does not have an equivalent to u_int32, so we use a 64 bit long,
		// thus avoiding signed/unsigned conversion errors.
		width = 0;
		for (int i = 0; i < 4; i++) {
			width *= (int) Math.pow(2, 8);
			width += header.getByte(i);
		}

		System.out.println("Width: " + width);

		height = 0;
		for (int i = 4; i < 8; i++) {
			height *= (int) Math.pow(2, 8);
			height += header.getByte(i);
		}

		System.out.println("Height: " + height);

		bitDepth = header.getByte(8);
		System.out.println("Bit Depth: " + bitDepth);

		colorType = header.getByte(9);
		System.out.println("Color Type: " + colorType);

		// Check to see if the bit depth/color type combination is a valid one.
		if (!contains(allowedBitDepths[colorTypeLookupTable[colorType]], bitDepth)) {
			System.out.println("Bit depth/color type combination is illegal.");
			status = -1;
			return;
		}
		System.out.println("Bit depth/color type combination is legal.");

		compression = header.getByte(10);
		System.out.println("Compression method: " + compression);

		filter = header.getByte(11);
		System.out.println("Filter: " + filter);

		interlace = header.getByte(12);
		System.out.println("Interlace method: " + interlace);

		System.out.println("Getting color information.");
		if (findChunk(sRGB) != null) {
			usessRGB = true;
			System.out.println("Uses sRGB standard.");
		}

		// *******************************************************************************************************
		// Getting gamma value
		// *******************************************************************************************************
		// Todo: implement; Not yet implemented
		Chunk gama = findChunk(GAMA);
		if (gama != null) {
			long g = 0;
			for (int i = 0; i < 4; i++) {
				g *= Math.pow(2, 8);
				g += gama.getByte(i);
			}
			gamma = g / 100000.0;
		}

		// *******************************************************************************************************
		// Getting IDAT chunks.
		// *******************************************************************************************************

		output = new BufferedImage((int) width, (int) height, BufferedImage.TYPE_3BYTE_BGR);
		BufferedImage output2 = new BufferedImage((int) width, (int) height, BufferedImage.TYPE_3BYTE_BGR);

		// Get ALL IDAT chunks.
		ArrayList<Chunk> IDATs = findChunks(IDAT);
		if (IDATs.size() == 0) {
			System.out.println("Fail. No IDAT chunks detected.");
			status = -2;
			return;
		}

		// *******************************************************************************************************
		// Setting up byte stream for the inflate class
		// *******************************************************************************************************

		// IDAT data is compressed with the zlib standard. Java offers a built in class
		// to decompress this data.
		// As such, since zlib is outside the scope of this project, I will use the
		// built in library for this.

		// IDAT information can be split among different chunks, and as such it is
		// required to take note of this.
		// Creating a byte buffer so as to be able to send all data to the inflate
		// algorithm.

		long compressedByteCount = 0;
		// Find exactly how long the compressed data is
		for (int i = 0; i < IDATs.size(); i++) {
			compressedByteCount += IDATs.get(i).length;
		}

		byte[] compressedData = new byte[(int) compressedByteCount];
		ByteBuffer bb = ByteBuffer.wrap(compressedData);

		System.out.println("This file contains " + compressedByteCount + " bytes of compressed image data. Across "
				+ IDATs.size() + " IDAT chunk(s).");

		// Copy entire dataset into the new ByteBuffer.

		int idatChunkProcess = 0;
		int offset = 0;
		Chunk processingChunk = IDATs.get(0);

		for (int i = 0; i < compressedByteCount; i++) {

			if (i - offset >= processingChunk.data.size()) {
				offset = i;
				idatChunkProcess++;
				try {
					processingChunk = IDATs.get(idatChunkProcess);
				} catch (Exception e) {
					// System.out.println("i: " + i + " data");
				}
			}

			bb.put((byte) processingChunk.getByte(i - offset));
		}

		bb.position(0);

		Inflater inflate = new Inflater();

		inflate.setInput(bb);
		byte[] decompressed = new byte[1];
		// Compressed data stream is now set up to be sent to the inflator.

		// *******************************************************************************************************
		// Find BKGD chunk if present
		// *******************************************************************************************************
		System.out.print("Searching for BKGD chunk....");
		Chunk bkgd = findChunk(BKGD);

		// Process background.
		if (bkgd != null) {
			System.out.println("Found.");
			switch (colorType) {
			case 2:
				if (bitDepth == 8)
					background.set(bkgd.getByte(0), bkgd.getByte(1), bkgd.getByte(2), bitDepth);
				else {
					long r, g, b;
					r = (long) (bkgd.getByte(0) * Math.pow(2, 8)) + bkgd.getByte(1);
					g = (long) (bkgd.getByte(2) * Math.pow(2, 8)) + bkgd.getByte(3);
					b = (long) (bkgd.getByte(4) * Math.pow(2, 8)) + bkgd.getByte(4);
				}
				break;

			case 3:
				background = pt.getColor(bkgd.getByte(0));
				break;

			case 4:

			default:
				System.out.println("Unsupported color type. Shouldn't be here.");
			}
		} else
			System.out.println("Not present.");

		// *******************************************************************************************************
		// Begin processing image.
		// *******************************************************************************************************

		// *******************************************************************************************************
		// Color type 2 R,G,B triple
		// *******************************************************************************************************

		if (colorType == 2) {
			// Color type 6 can have either a bit depth of 8 or 16
			ColorTriple ct = new ColorTriple(0, 0, 0);
			for (int y = 0; y < height; y++) {
				// Discard first byte of each scanline.
				inflate.inflate(decompressed);
				int filter = decompressed[0];
				ColorTriple temp = new ColorTriple(0);
				ColorTriple temp1 = new ColorTriple(0);
				ColorTriple blue = new ColorTriple(0, 0, 255);
				ColorTriple red = new ColorTriple(255, 0, 0);
				ColorTriple green = new ColorTriple(0, 255, 0);
				// System.out.println(filter);
				for (int x = 0; x < width; x++) {
					if (bitDepth == 8) // If the bit depth is 8, that's one byte per pixel per color.
					{
						inflate.inflate(decompressed);
						int r = Byte.toUnsignedInt(decompressed[0]);
						inflate.inflate(decompressed);
						int g = Byte.toUnsignedInt(decompressed[0]);
						inflate.inflate(decompressed);
						int b = Byte.toUnsignedInt(decompressed[0]);
						ct.set(r, g, b);

						// System.out.println(g);

						switch (filter) {
						case 0:
							// No filtering
							// System.out.println("Line " + y + " is a 0 filter");
							break;
						case 1:
							// System.out.println("Sub");
							// Sub filtering

							if (temp == null)
								temp = ct.clone();
							else {
								ct.add(temp);
								temp = ct.clone();
							}
							output2.setRGB(x, y, red.toInt());

							break;
						case 2:
							// Up filtering
							// System.out.println("Up");
							if (y > 0) {
								temp = new ColorTriple(output.getRGB(x, y - 1));
								ct.add(temp);
							}
							output2.setRGB(x, y, green.toInt());
							break;
						case 3:
							// Average
							// System.out.println("Average");
							if (x > 0)
								temp = new ColorTriple(output.getRGB(x - 1, y));
							if (y > 0)
								temp1 = new ColorTriple(output.getRGB(x, y - 1));
							temp = temp.average(temp1);
							output2.setRGB(x, y, blue.toInt());
							ct.add(temp);
							break;
						default:
							System.out.println("Er");
							ct.set(0, 0, 0);
						}

						// System.out.println(inflate.finished());

						if (gamma != 1) {
							// ct.gammaCorrect(gamma);
						}

						output.setRGB(x, y, ct.toInt());

					} else // If the bit depth is 16, need to process 16 bytes per pixel.
					{

					}
				}
			}
		}

		// *******************************************************************************************************
		// Color type 3 - Palate mode
		// *******************************************************************************************************

		if (colorType == 3) {
			System.out.print("Color type is 3, locating palette chunk...");
			Chunk palette = findChunk(PLTE);

			if (palette == null) {
				System.out.print("NOT found.");
				status = -2;
				return;
			}

			System.out.println("Found.");

			System.out.print("Validating chunk length...");
			if (palette.data.size() % 3 != 0) {
				System.out.println("Fail. Not divisible by 3. Is: " + palette.data.size());
				status = -2;
				return;
			}
			System.out.println("Pass.");

			// Find palette information if applicable.
			// As per PNG standard, color type 3 (The only one which uses the PLTE chunk)
			// the color depth is 3 bits per color.
			System.out.print("Creating palette...");
			pt = new palate();
			ArrayList<Integer> ptData = palette.data;
			System.out.println(ptData.size());
			for (int i = 0; i < ptData.size(); i += 3) {
				pt.addColor(new ColorTriple(ptData.get(i), ptData.get(i + 1), ptData.get(i + 2)));
				if (gamma != 1) {
					// pt.getColor(i/3).gammaCorrect(gamma);
				}
			}

			System.out.println("Complete. Contains " + pt.size() + " entries.");

			// For PNG standard; the amount of image data we expect to be output changes
			// depending on the bitdepth, as such it can be difficult to guage exactly how
			// large we need to make
			// an output array. To make it much easier on myself, I will be decompressing...
			// One byte at a time. Slow, ineffecient? Yes. Easier than in bulk? Yes.

			int oaat;

			// Begin decompression and setting pixels.
			for (int y = 0; y < height; y++) {
				// inflate.inflate(decompressed);

				inflate.inflate(decompressed); // Discard the first byte of every scan line.
				for (int x = 0; x < width; x++) {

					switch (bitDepth) {
					// case 1:

					// break;
					// case 2:

					// break;
					case 4:

						inflate.inflate(decompressed);
						oaat = Byte.toUnsignedInt(decompressed[0]);
						int upper4 = oaat / (int) Math.pow(2, 4);
						int lower4 = (oaat - (upper4 * (int) Math.pow(2, 4)));

						output.setRGB(x, y, pt.getColor(upper4).toInt());
						x++;
						if (x >= width) {
							x = 0;
							y++;
						}
						output.setRGB(x, y, pt.getColor(lower4).toInt());

						break;
					case 8:
						inflate.inflate(decompressed);
						oaat = Byte.toUnsignedInt(decompressed[0]);
						output.setRGB(x, y, pt.getColor(oaat).toInt());
						break;

					default:
						System.out.println("Unknown bit-depth.");
						return;
					}
				}
			}
		}

		// *******************************************************************************************************
		// Color type 6
		// *******************************************************************************************************
		ColorTriple temp = new ColorTriple(0);
		ColorTriple temp1 = new ColorTriple(0);
		ColorTriple blue = new ColorTriple(0, 0, 255);
		ColorTriple red = new ColorTriple(255, 0, 0);
		ColorTriple green = new ColorTriple(0, 255, 0);
		if (colorType == 6) {
			// Color type 6 can have either a bit depth of 8 or 16
			ColorTriple ct = new ColorTriple(0, 0, 0);
			for (int y = 0; y < height; y++) {
				// Discard first byte of each scanline.

				inflate.inflate(decompressed);
				int filter = decompressed[0];
				if (y == 0)
					System.out.println("y0 filter: " + filter);
				for (int x = 0; x < width; x++) {
					if (bitDepth == 8) // If the bit depth is 8, that's one byte per pixel per color.
					{
						inflate.inflate(decompressed);
						int r = (int) decompressed[0];
						inflate.inflate(decompressed);
						int g = (int) decompressed[0];
						inflate.inflate(decompressed);
						int b = (int) decompressed[0];

						// One alpha that needs to be discarded.
						inflate.inflate(decompressed);

						ct.set(r, g, b);

						if (gamma != 1) {
							// ct.gammaCorrect(gamma);
						}

						output.setRGB(x, y, ct.toInt());

					} else // If the bit depth is 16, need to process 16 bits per color per pixel.
					{
						inflate.inflate(decompressed);
						long r = Byte.toUnsignedLong(decompressed[0]);
						r *= Math.pow(2, 8);

						inflate.inflate(decompressed);
						r += Byte.toUnsignedLong(decompressed[0]);

						inflate.inflate(decompressed);
						long g = Byte.toUnsignedLong(decompressed[0]);
						g *= Math.pow(2, 8);
						inflate.inflate(decompressed);
						g += Byte.toUnsignedLong(decompressed[0]);

						inflate.inflate(decompressed);
						long b = Byte.toUnsignedLong(decompressed[0]);

						b *= Math.pow(2, 8);

						inflate.inflate(decompressed);

						b += Byte.toUnsignedLong(decompressed[0]);

						ct = new ColorTriple(r, g, b, bitDepth);

						switch (filter) {
						case 0:
							// No filtering
							// System.out.println("Line " + y + " is a 0 filter");
							break;
						case 1:
							// System.out.println("Sub");
							// Sub filtering

							if (temp == null)
								temp = ct.clone();
							else {
								long ra = (long) (ColorTriple.getUpperByte((int) r)
										+ ColorTriple.getUpperByte((int) temp.R) % 256) * (int) Math.pow(2, 16)
										+ (ColorTriple.getLowerByte((int) r)
												+ ColorTriple.getLowerByte((int) temp.R) % 256);

								long ga = (long) (ColorTriple.getUpperByte((int) g)
										+ ColorTriple.getUpperByte((int) temp.G) % 256) * (int) Math.pow(2, 16)
										+ (ColorTriple.getLowerByte((int) g)
												+ ColorTriple.getLowerByte((int) temp.G) % 256);

								long ba = (long) (ColorTriple.getUpperByte((int) b)
										+ ColorTriple.getUpperByte((int) temp.B) % 256) * (int) Math.pow(2, 16)
										+ (ColorTriple.getLowerByte((int) b)
												+ ColorTriple.getLowerByte((int) temp.B) % 256);

								ct.set(ra, ga, ba, bitDepth);

								temp = ct.clone();

							}
							output2.setRGB(x, y, green.toInt());

							break;
						case 2:
							// Up filtering
							// System.out.println("Up");
							temp = new ColorTriple(output.getRGB(x, y - 1));
							temp.R = (long) temp.sR * (long) (Math.pow(2, 16) - 1);
							temp.G = (long) temp.sG * (long) (Math.pow(2, 16) - 1);
							temp.B = (long) temp.sB * (long) (Math.pow(2, 16) - 1);
							temp.process();
							long ra = (long) (ColorTriple.getUpperByte((int) r)
									+ ColorTriple.getUpperByte((int) temp.R) % 256) * (int) Math.pow(2, 16)
									+ (ColorTriple.getLowerByte((int) r)
											+ ColorTriple.getLowerByte((int) temp.R) % 256);

							long ga = (long) (ColorTriple.getUpperByte((int) g)
									+ ColorTriple.getUpperByte((int) temp.G) % 256) * (int) Math.pow(2, 16)
									+ (ColorTriple.getLowerByte((int) g)
											+ ColorTriple.getLowerByte((int) temp.G) % 256);

							long ba = (long) (ColorTriple.getUpperByte((int) b)
									+ ColorTriple.getUpperByte((int) temp.B) % 256) * (int) Math.pow(2, 16)
									+ (ColorTriple.getLowerByte((int) b)
											+ ColorTriple.getLowerByte((int) temp.B) % 256);

							ct.set(ra, ga, ba, bitDepth);
							output2.setRGB(x, y, green.toInt());
							break;
						case 3:
							// Average
							// System.out.println("Average");
							if (x > 0) {
								temp = new ColorTriple(output.getRGB(x - 1, y));
								temp.R = (long) temp.sR * (long) (Math.pow(2, 16) - 1);
								temp.G = (long) temp.sG * (long) (Math.pow(2, 16) - 1);
								temp.B = (long) temp.sB * (long) (Math.pow(2, 16) - 1);
								temp.process();
							}
							if (y > 0) {
								temp1 = new ColorTriple(output.getRGB(x, y - 1));
								temp1.R = (long) temp1.sR * (long) (Math.pow(2, 16) - 1);
								temp1.G = (long) temp1.sG * (long) (Math.pow(2, 16) - 1);
								temp1.B = (long) temp1.sB * (long) (Math.pow(2, 16) - 1);
								temp1.process();
							}

							ra = (long) (ColorTriple.getUpperByte((int) r)
									+ (ColorTriple.getUpperByte((int) temp.R) + ColorTriple.getUpperByte((int) temp1.R))
											/ 2 % 256)
									* (int) Math.pow(2, 16)
									+ (ColorTriple.getLowerByte((int) r) + (ColorTriple.getLowerByte((int) temp.R)
											+ ColorTriple.getLowerByte((int) temp1.R)) / 2 % 256);

							ga = (long) (ColorTriple.getUpperByte((int) g)
									+ (ColorTriple.getUpperByte((int) temp.G) + ColorTriple.getUpperByte((int) temp1.G))
											/ 2 % 256)
									* (int) Math.pow(2, 16)
									+ (ColorTriple.getLowerByte((int) g) + (ColorTriple.getLowerByte((int) temp.G)
											+ ColorTriple.getLowerByte((int) temp1.G)) / 2 % 256);

							ba = (long) (ColorTriple.getUpperByte((int) b)
									+ (ColorTriple.getUpperByte((int) temp.B) + ColorTriple.getUpperByte((int) temp1.B))
											/ 2 % 256)
									* (int) Math.pow(2, 16)
									+ (ColorTriple.getLowerByte((int) b) + (ColorTriple.getLowerByte((int) temp.B)
											+ ColorTriple.getLowerByte((int) temp1.B)) / 2 % 256);

							output2.setRGB(x, y, blue.toInt());
							ct.set(ra, ga, ba);
							break; // a left b above c upper left
						case 4:
							output2.setRGB(x, y, red.toInt());
							// PaethPredictor
							ColorTriple l = new ColorTriple(0);
							ColorTriple a;
							ColorTriple ul = new ColorTriple(0);
							if (x != 0) {
								l = new ColorTriple(output.getRGB(x - 1, y));
								l.R = (long) l.sR * (long) (Math.pow(2, 16) - 1);
								l.G = (long) l.sG * (long) (Math.pow(2, 16) - 1);
								l.B = (long) l.sB * (long) (Math.pow(2, 16) - 1);
								l.process();
								ul = new ColorTriple(output.getRGB(x - 1, y - 1));
								ul.R = (long) ul.sR * (long) (Math.pow(2, 16) - 1);
								ul.G = (long) ul.sG * (long) (Math.pow(2, 16) - 1);
								ul.B = (long) ul.sB * (long) (Math.pow(2, 16) - 1);
								ul.process();
							}
							a = new ColorTriple(output.getRGB(x, y - 1));

							long rUp = PaethPredictor(ColorTriple.getUpperByte((int) l.R),
									ColorTriple.getUpperByte((int) a.R), ColorTriple.getUpperByte((int) ul.R));
							long gUp = PaethPredictor(ColorTriple.getUpperByte((int) l.R),
									ColorTriple.getUpperByte((int) a.R), ColorTriple.getUpperByte((int) ul.R));
							long bUp = PaethPredictor(ColorTriple.getUpperByte((int) l.G),
									ColorTriple.getUpperByte((int) a.G), ColorTriple.getUpperByte((int) ul.G));
							
							rUp *= Math.pow(2, 16);
							gUp *= Math.pow(2, 16);
							bUp *= Math.pow(2, 16);
							
							rUp += PaethPredictor(ColorTriple.getLowerByte((int) l.R),
									ColorTriple.getLowerByte((int) a.R), ColorTriple.getLowerByte((int) ul.R));
							gUp += PaethPredictor(ColorTriple.getLowerByte((int) l.G),
									ColorTriple.getLowerByte((int) a.G), ColorTriple.getLowerByte((int) ul.G));
							bUp += PaethPredictor(ColorTriple.getLowerByte((int) l.B),
									ColorTriple.getLowerByte((int) a.B), ColorTriple.getLowerByte((int) ul.B));
							// System.out.println("Who is it");

							a.set(rUp, gUp, b);

							ct.add(a);
							break;
						default:
							System.out.println("Er");
							ct.set(0, 0, 0);
						}

						// Two alpha bytes that needs to be discarded.
						inflate.inflate(decompressed);
						inflate.inflate(decompressed);

						// if (ct.toString().equals("0 0 0"))
						// System.out.println(ct.toString());

						if (gamma != 1) {
							// ct.gammaCorrect(gamma);
						}

						output.setRGB(x, y, ct.toInt());
					}
				}
			}
		}

		System.out.println("Finished: " + inflate.finished());

		int count = 0;
		while (inflate.inflate(decompressed) != 0)
			count++;
		//System.out.println("Count: " + count);

		JFrame frame = new JFrame();
		frame.getContentPane().setLayout(new FlowLayout());
		frame.getContentPane().add(new JLabel(new ImageIcon(output)));
		frame.pack();
		frame.setVisible(true);
		JFrame frame2 = new JFrame();
		frame2.getContentPane().setLayout(new FlowLayout());
		frame2.getContentPane().add(new JLabel(new ImageIcon(output2)));
		frame2.pack();
		//frame2.setVisible(true);
		while (true)
			;
	}

	// PaethPredictor function.
	// Taken from PNG 1.2 specification.
	private long PaethPredictor(long a1, long b1, long c1) {
		long ret = a1 + b1 - c1;
		long pa = Math.abs(ret - a1);
		long pb = Math.abs(ret - b1);
		long pc = Math.abs(ret - c1);

		if (pa <= pb && pa <= pc)
			return a1;
		if (pb <= pc)
			return b1;
		return c1;
	}

	// A way to get if the bit in position "bit" from int "a" is 0 or 1 - Used in
	// calculating color values for bit depths < 8
	private int getBitFromInt(int a, int bit) {
		byte b = (byte) a;
		byte c = (byte) (bit * (int) Math.pow(2, bit));
		if ((a & b) == 0)
			return 0;
		else
			return 1;
	}

	// A way for the chunk class to tell to stop looking for new chunks.
	public void callStop() {
		stop = true;
	}

	// Easy way of reading in X number of integers. Since it's reading in chars,
	// only the lower 8 bytes are used.
	// Overload: Static method to allow chunks to make use of this method as well.
	private int[] getXInts(long X) throws IOException {

		ArrayList<Integer> c = new ArrayList<>();

		for (long i = 0; i < X; i++) {

			int read = reader.read();

			if (read == -1)
				return null;

			c.add(read);

		}

		int[] ret = new int[c.size()];
		for (int i = 0; i < c.size(); i++)
			ret[i] = c.get(i);

		return ret;
	}

	public static int[] getXInts(FileInputStream fis, long X) throws IOException {

		ArrayList<Integer> c = new ArrayList<>();

		for (long i = 0; i < X; i++) {

			int read = fis.read();

			if (read == -1)
				return null;

			c.add(read);

		}

		int[] ret = new int[c.size()];
		for (int i = 0; i < c.size(); i++)
			ret[i] = c.get(i);

		return ret;
	}

	// Easy way to compare the integer arrays- Java does a terrible job at it
	// natively.
	private boolean compareIntArrays(int[] a, int[] b) {
		if (a.length != b.length)
			return false;
		for (int i = 0; i < a.length; i++)
			if (a[i] != b[i])
				return false;
		return true;
	}

	// Easy way to check if table a contains value b;
	private boolean contains(int[] a, int b) {
		for (int i = 0; i < a.length; i++)
			if (a[i] == b)
				return true;
		return false;
	}

	// Convert a 4-byte integer array to a long.
	private long intArrayToLong(int[] a) {
		long ret = 0;
		for (int i = 0; i < 4; i++) {
			ret *= (int) Math.pow(2, 8);
			ret += a[i];
		}
		return ret;
	}

	// Easy way to find chunk of type "c" where c is a 4-int array.
	private Chunk findChunk(int[] c) {
		long test = intArrayToLong(c);
		for (int i = 0; i < chunks.size(); i++) {
			if (chunks.get(i).type == test)
				return chunks.get(i);
		}
		return null;
	}

	// Easy way to find all chunks of type "c" where c is a 4-int array
	private ArrayList<Chunk> findChunks(int[] c) {
		long test = intArrayToLong(c);
		ArrayList<Chunk> subset = new ArrayList<>();
		for (int i = 0; i < chunks.size(); i++) {
			if (chunks.get(i).type == test)
				subset.add(chunks.get(i));
		}
		return subset;
	}

	// Each chunk is 4 bytes in length. Create a simple object that will keep track
	// of each individual chunk.
	private class Chunk {
		public long length;
		public long type;
		public long CRC;
		public ArrayList<Integer> data;
		public boolean complete = false;

		// Constructor assumes that the input stream is perfectly at the start of a new
		// chunk.
		public Chunk(FileInputStream fr, PNG caller) throws IOException {
			// System.out.println("New Chunk.");

			int[] l = PNG.getXInts(fr, 4); // Gets length of chunk.
			for (int i = 0; i < 4; i++) {
				length *= (int) Math.pow(2, 8);
				length += l[i];
				// System.out.println(l[i]);
			}

			// System.out.println("length: " + Long.toUnsignedString(length));
			l = PNG.getXInts(fr, 4); // Gets type of chunk.

			if (compareIntArrays(IEND, l)) {
				// System.out.println("This chunk is IEND. Terminating chunk collection.");
				caller.callStop();
				complete = true;
				return;
			}

			// IHDR chunk check. Used for debugging.
			if (compareIntArrays(IHDR, l)) {
				// System.out.println("This chunk is IHDR");
			}

			// Again, Java has no support for u_int32 - Needed a workaround.
			for (int i = 0; i < 4; i++) {
				type *= (int) Math.pow(2, 8);
				type += l[i];
				// System.out.println(a[i]);
			}
			// System.out.println("type: " + Long.toUnsignedString(type));

			data = new ArrayList<>();

			for (long i = 0; i < length; i++) {
				data.add(PNG.getXInts(fr, 1)[0]);
				// System.out.println("Added data: " + data.get(data.size()-1));
			}

			l = PNG.getXInts(fr, 4); // Gets length of chunk.
			for (int i = 0; i < 4; i++) {
				CRC *= (int) Math.pow(2, 8);
				CRC += l[i];
				// System.out.println(l[i]);
			}
			// System.out.println("CRC: " + CRC);

			// System.out.println("End Chunk.");

			complete = true;

		}

		public int getByte(int location) {
			return data.get(location);
		}

	}

}
