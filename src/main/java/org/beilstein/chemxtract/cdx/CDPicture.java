/*
 * Copyright (c) 2025-2030 Beilstein-Institut
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */
package org.beilstein.chemxtract.cdx;

/**
 * This object represents an externally-generated object embedded in the
 * file, usually a metafile or OLE object.
 */
public class CDPicture extends CDObject {
  private float rotationAngle;

  private byte[] pictureEdition;
  private byte[] pictureEditionAlias;
  private byte[] macPICT;
  private byte[] windowsMetafile;
  private byte[] oleObject;
  private byte[] enhancedMetafile;
  private byte[] gif;
  private byte[] tiff;
  private byte[] png;
  private byte[] jpeg;
  private byte[] bmp;

  public float getRotationAngle() {
    return rotationAngle;
  }

  public void setRotationAngle(float rotationAngle) {
    this.rotationAngle = rotationAngle;
  }

  public byte[] getPictureEdition() {
    return pictureEdition;
  }

  public void setPictureEdition(byte[] pictureEdition) {
    this.pictureEdition = pictureEdition;
  }

  public byte[] getPictureEditionAlias() {
    return pictureEditionAlias;
  }

  public void setPictureEditionAlias(byte[] pictureEditionAlias) {
    this.pictureEditionAlias = pictureEditionAlias;
  }

  public byte[] getMacPICT() {
    return macPICT;
  }

  public void setMacPICT(byte[] macPICT) {
    this.macPICT = macPICT;
  }

  public byte[] getWindowsMetafile() {
    return windowsMetafile;
  }

  public void setWindowsMetafile(byte[] windowsMetafile) {
    this.windowsMetafile = windowsMetafile;
  }

  public byte[] getOleObject() {
    return oleObject;
  }

  public void setOleObject(byte[] oleObject) {
    this.oleObject = oleObject;
  }

  public byte[] getEnhancedMetafile() {
    return enhancedMetafile;
  }

  public void setEnhancedMetafile(byte[] enhancedMetafile) {
    this.enhancedMetafile = enhancedMetafile;
  }

  public byte[] getGif() {
    return gif;
  }

  public void setGif(byte[] gif) {
    this.gif = gif;
  }

  public byte[] getTiff() {
    return tiff;
  }

  public void setTiff(byte[] tiff) {
    this.tiff = tiff;
  }

  public byte[] getPng() {
    return png;
  }

  public void setPng(byte[] png) {
    this.png = png;
  }

  public byte[] getJpeg() {
    return jpeg;
  }

  public void setJpeg(byte[] jpeg) {
    this.jpeg = jpeg;
  }

  public byte[] getBmp() {
    return bmp;
  }

  public void setBmp(byte[] bmp) {
    this.bmp = bmp;
  }

  @Override
  public void accept(CDVisitor visitor) {
    visitor.visitEmbeddedObject(this);
    super.accept(visitor);
  }

}
