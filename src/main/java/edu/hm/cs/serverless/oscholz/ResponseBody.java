package edu.hm.cs.serverless.oscholz;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Body representing the response payload
 *
 * @author Oliver Scholz
 */
public class ResponseBody {

	private final VmInfo vmInfo;
	private final Map<String, String> osRelease;
	private final String location;
	private final Map<String, String> cmdResults;
	private final Map<String, String[]> directories;

	public ResponseBody(VmInfo vmInfo, Map<String, String> osRelease, String location, Map<String, String> cmdResults, Map<String, String[]> directories) {
		this.vmInfo = vmInfo;
		this.osRelease = osRelease;
		this.location = location;
		this.cmdResults = cmdResults;
		this.directories = directories;
	}

	public VmInfo getVmInfo() {
		return vmInfo;
	}

	public Map<String, String> getOsRelease() {
		return osRelease;
	}

	public String getLocation() {
		return location;
	}

	public Map<String, String> getCmdResults() {
		return cmdResults;
	}

	public Map<String, String[]> getDirectories() {
		return directories;
	}

	public Map<String, String> getDirectoriesStringOnly() {
		Map<String, String> map = new HashMap<>();
		directories.forEach((k, v) -> {
			map.put(k, Arrays.toString(v));
		});
		return map;
	}

	@Override
	public String toString() {
		return "ResponseBody{" +
				"vmInfo=" + vmInfo +
				", osRelease=" + osRelease +
				", location='" + location + '\'' +
				", cmdResults=" + cmdResults +
				", directories=" + getDirectoriesStringOnly() +
				'}';
	}
}
