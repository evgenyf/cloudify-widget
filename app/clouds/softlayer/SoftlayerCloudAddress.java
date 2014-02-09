package clouds.softlayer;

import clouds.base.CloudAddress;


/**
 * Softlayer implementation of CloudAddress
 * @author evgenyf
 * Date: 10/7/13
 */
public class SoftlayerCloudAddress implements CloudAddress{

	private final String address;

	public SoftlayerCloudAddress(String address){
		this.address = address;
	}

	@Override
	public String getAddr() {
		return address;
	}

	@Override
	public String toString() {
		return "SoftlayerCloudAddress [address=" + address + "]";
	}
}