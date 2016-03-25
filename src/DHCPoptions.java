import java.io.*;
import java.util.*;

/**
 * @author Gilles
 *
 */

public class DHCPoptions {
	private Map<Integer, byte[]> options = new HashMap<Integer, byte[]>();
	
	/**
	 * Constructor for DHCPoptions objects
	 */
	public DHCPoptions(){
	}

	/**
	 * Convert DHCPoptions object to a byte array
	 * @return byte array containing the options from this DHCPoptions object
	 */
	public byte[] toByteArray(){
		ByteArrayOutputStream data = new ByteArrayOutputStream ();
		try {
			data.write((byte) 99);
			data.write((byte) 130);
			data.write((byte) 83);
			data.write((byte) 99);
			for (Map.Entry<Integer, byte[]> entry : this.getOptions().entrySet()){
				data.write((byte)(int) entry.getKey());
				data.write((byte) entry.getValue().length);
				data.write(entry.getValue());
			}
			data.write((byte) 255);
			} catch (IOException e) {}
		byte[] option = data.toByteArray();
		return option;
	}
	
	/**
	 * Read options data from a byte array and store it into this DHCPoptions object
	 * @param dataArray
	 */
	
	public void fromByteArray(byte[] dataArray){
		this.getOptions().clear();
		System.out.println(Arrays.toString(dataArray));
		byte[] dataclip = Arrays.copyOfRange(dataArray, 4, dataArray.length);
		ByteArrayInputStream data = new ByteArrayInputStream(dataclip);
		byte[] code = new byte[1], length = new byte[1];
		whileloop:
		while(true){
			try {
				int read = data.read(code);
				// System.out.println(Arrays.toString(code));
				if ((int) code[0] == 255 || (int) code[0] == -1 || read == -1){
					break whileloop;
				}
				data.read(length);
				byte[] readData = new byte[(int) length[0]];
				data.read(readData);
				this.updateOption((int) code[0], readData);				
			} catch (IOException ex) {
				System.err.println(ex);
			}
		}
	}
	
	/**
	 * Add a specified option or update it's value
	 * @param optCode the option code
	 * @param data the data to be put in the option field
	 * @post the option supplied through it's code and data is added to the options map
	 */
	public void updateOption(int optCode, byte[] data){
		options.put(optCode, data);
	}
	
	/**
	 * Remove a specified option
	 * @param optCode the option code
	 * @post the specified option is cleared from the options map
	 */
	public void removeOption(int optCode){
		options.remove(optCode);
	}
	
	/**
	 * Retreive the current data stored in option with the specified option code
	 * @param optCode the specified option code
	 * @return a byte array containing the data for the specified option
	 */
	public byte[] getOption(int optCode){
		return options.get(optCode);
	}
	
	/**
	 * @return the options
	 */
	private Map<Integer, byte[]> getOptions() {
		return options;
	}
}
