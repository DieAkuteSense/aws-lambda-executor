package edu.hm.cs.serverless.oscholz;

/**
 * Body holding the {@link ResponseBody}
 *
 * @author Oliver Scholz
 */
public class Body {
	ResponseBody body;

	public ResponseBody getBody() {
		return body;
	}

	public void setBody(ResponseBody body) {
		this.body = body;
	}

	@Override
	public String toString() {
		return "Body{" +
				"body=" + body +
				'}';
	}
}
