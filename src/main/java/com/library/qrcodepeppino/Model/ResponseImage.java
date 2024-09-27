package com.library.qrcodepeppino.Model;

public class ResponseImage {
    private String imageBase64;

    public ResponseImage() {
    }

    public String getImageBase64() {
        return this.imageBase64;
    }

    public void setImageBase64(final String imageBase64) {
        this.imageBase64 = imageBase64;
    }

    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof ResponseImage other)) {
            return false;
        } else {
            if (!other.canEqual(this)) {
                return false;
            } else {
                Object this$imageBase64 = this.getImageBase64();
                Object other$imageBase64 = other.getImageBase64();
                if (this$imageBase64 == null) {
                    if (other$imageBase64 != null) {
                        return false;
                    }
                } else if (!this$imageBase64.equals(other$imageBase64)) {
                    return false;
                }

                return true;
            }
        }
    }

    protected boolean canEqual(final Object other) {
        return other instanceof ResponseImage;
    }

    public int hashCode() {
        int result = 1;
        Object $imageBase64 = this.getImageBase64();
        result = result * 59 + ($imageBase64 == null ? 43 : $imageBase64.hashCode());
        return result;
    }

    public String toString() {
        return "ResponseImage(imageBase64=" + this.getImageBase64() + ")";
    }
}