package pip.dimens;

public class MaskModel {
	public int height, width, ColorCode;
	public float zoom;

	public MaskModel(int w, int h, int colorDot, float zoom) {
		width = w;
		height = h;
		this.zoom = zoom;
		ColorCode = colorDot;
	}
}
