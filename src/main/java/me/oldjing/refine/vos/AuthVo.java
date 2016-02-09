package me.oldjing.refine.vos;

public class AuthVo extends BasicVo {
	public final SidVo data;

	public AuthVo(SidVo data) {
		this.data = data;
	}

	public static class SidVo {
		public final String sid;
		public final boolean is_portal_port;
		public final String did;

		public SidVo(String sid, boolean is_portal_port, String did) {
			this.sid = sid;
			this.is_portal_port = is_portal_port;
			this.did = did;
		}
	}
}
