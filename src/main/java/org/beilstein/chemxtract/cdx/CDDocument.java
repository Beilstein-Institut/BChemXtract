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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.beilstein.chemxtract.cdx.datatypes.CDPoint2D;

/** The root of the CDX object model. Contains at least one page object. */
public class CDDocument {
  /** The magnification level for the control as a percentage. */
  private float magnification;

  /** The name of the document. */
  private String name;

  /** The settings object. */
  private final CDSettings settings = new CDSettings();

  private List<CDPage> pages = new ArrayList<>();
  private CDTemplateGrid templateGrid;

  private String creationUserName;
  private Date creationDate;
  private String creationProgram;
  private String modificationUserName;
  private Date modificationDate;
  private String modificationProgram;
  private String comment;
  private CDRectangle boundingBox;
  private byte[] macPrintInfo;
  private byte[] winPrintInfo;
  private CDRectangle printMargins;
  private boolean fractionalWidths = false;
  private CDPoint2D fixInPlaceExtent;
  private CDPoint2D fixInPlaceGap;
  private boolean windowIsZoomed = false;
  private CDPoint2D windowPosition;
  private CDPoint2D windowSize;
  private byte[] cartridgeData;

  public String getCreationUserName() {
    return creationUserName;
  }

  public void setCreationUserName(String creationUserName) {
    this.creationUserName = creationUserName;
  }

  public Date getCreationDate() {
    return creationDate;
  }

  public void setCreationDate(Date creationDate) {
    this.creationDate = creationDate;
  }

  public String getCreationProgram() {
    return creationProgram;
  }

  public void setCreationProgram(String creationProgram) {
    this.creationProgram = creationProgram;
  }

  public String getModificationUserName() {
    return modificationUserName;
  }

  public void setModificationUserName(String modificationUserName) {
    this.modificationUserName = modificationUserName;
  }

  public Date getModificationDate() {
    return modificationDate;
  }

  public void setModificationDate(Date modificationDate) {
    this.modificationDate = modificationDate;
  }

  public String getModificationProgram() {
    return modificationProgram;
  }

  public void setModificationProgram(String modificationProgram) {
    this.modificationProgram = modificationProgram;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public CDRectangle getBoundingBox() {
    return boundingBox;
  }

  public void setBoundingBox(CDRectangle boundingBox) {
    this.boundingBox = boundingBox;
  }

  public CDRectangle getPrintMargins() {
    return printMargins;
  }

  public void setPrintMargins(CDRectangle printMargins) {
    this.printMargins = printMargins;
  }

  public boolean isFractionalWidths() {
    return fractionalWidths;
  }

  public void setFractionalWidths(boolean fractionalWidths) {
    this.fractionalWidths = fractionalWidths;
  }

  public float getMagnification() {
    return magnification;
  }

  public void setMagnification(float magnification) {
    this.magnification = magnification;
  }

  public CDPoint2D getFixInPlaceExtent() {
    return fixInPlaceExtent;
  }

  public void setFixInPlaceExtent(CDPoint2D fixInPlaceExtent) {
    this.fixInPlaceExtent = fixInPlaceExtent;
  }

  public CDPoint2D getFixInPlaceGap() {
    return fixInPlaceGap;
  }

  public void setFixInPlaceGap(CDPoint2D fixInPlaceGap) {
    this.fixInPlaceGap = fixInPlaceGap;
  }

  public boolean isWindowIsZoomed() {
    return windowIsZoomed;
  }

  public void setWindowIsZoomed(boolean windowIsZoomed) {
    this.windowIsZoomed = windowIsZoomed;
  }

  public CDPoint2D getWindowPosition() {
    return windowPosition;
  }

  public void setWindowPosition(CDPoint2D windowPosition) {
    this.windowPosition = windowPosition;
  }

  public CDPoint2D getWindowSize() {
    return windowSize;
  }

  public void setWindowSize(CDPoint2D windowSize) {
    this.windowSize = windowSize;
  }

  public List<CDPage> getPages() {
    return pages;
  }

  public void setPages(List<CDPage> pages) {
    this.pages = pages;
  }

  public byte[] getMacPrintInfo() {
    return macPrintInfo;
  }

  public void setMacPrintInfo(byte[] macPrintInfo) {
    this.macPrintInfo = macPrintInfo;
  }

  public byte[] getWinPrintInfo() {
    return winPrintInfo;
  }

  public void setWinPrintInfo(byte[] winPrintInfo) {
    this.winPrintInfo = winPrintInfo;
  }

  public byte[] getCartridgeData() {
    return cartridgeData;
  }

  public void setCartridgeData(byte[] cartridgeData) {
    this.cartridgeData = cartridgeData;
  }

  public CDTemplateGrid getTemplateGrid() {
    return templateGrid;
  }

  public void setTemplateGrid(CDTemplateGrid templateGrid) {
    this.templateGrid = templateGrid;
  }

  public CDSettings getSettings() {
    return settings;
  }
}
