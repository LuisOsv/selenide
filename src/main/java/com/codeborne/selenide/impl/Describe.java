package com.codeborne.selenide.impl;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.By;
import org.openqa.selenium.InvalidElementStateException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

import static com.codeborne.selenide.Selenide.executeJavaScript;

public class Describe {
  private WebElement element;
  private StringBuilder sb = new StringBuilder();

  Describe(WebElement element) {
    this.element = element;
    sb.append('<').append(element.getTagName());
  }

  Describe appendAttributes() {
    String attributes = executeJavaScript(
        "var s = ''; var attrs = arguments[0].attributes; for (var i=0; i<attrs.length; ++i) { var a = attrs[i]; s += ' \"' + a.name + '=' + a.value + \"; } ; return s;", element);
    sb.append(attributes);
    return this;
  }
  
  Describe attr(String attributeName) {
    String attributeValue = element.getAttribute(attributeName);
    if (attributeValue != null && attributeValue.length() > 0) {
      sb.append(' ').append(attributeName).append('=').append(attributeValue);
    }
    return this;
  }

  Describe is(String name, boolean value, boolean valueToShow) {
    if (value == valueToShow) {
      sb.append(' ').append(name).append(':').append(value);
    }
    return this;
  }

  @Override
  public String toString() {
    String text = element.getText();
    sb.append('>').append(text == null ? "" : text).append("</").append(element.getTagName()).append('>');
    return sb.toString();
  }

  public String flush() {
    return sb.append('>').toString();
  }

  public static String describe(WebElement element) {
    try {
      if (element == null) {
        return "null";
      }
      return new Describe(element)
          .appendAttributes()
//          .attr("id").attr("name").attr("class").attr("href").attr("value").attr("disabled")
//          .attr("type").attr("placeholder")
//          .attr("onclick").attr("onClick").attr("onchange").attr("onChange")
          .is("selected", isSelected(element), true)
          .is("displayed", element.isDisplayed(), false)
          .is("enabled", element.isEnabled(), false)
          .toString();
    } catch (WebDriverException elementDoesNotExist) {
      return Cleanup.of.webdriverExceptionMessage(elementDoesNotExist);
    }
    catch (IndexOutOfBoundsException e) {
      return e.toString();
    }
  }

  public static String shortly(WebElement element) {
    try {
      if (element == null) {
        return "null";
      }
      if (element instanceof SelenideElement) {
        return shortly(((SelenideElement) element).toWebElement());
      }
      return new Describe(element).attr("id").attr("name").flush();
    } catch (WebDriverException elementDoesNotExist) {
      return Cleanup.of.webdriverExceptionMessage(elementDoesNotExist);
    }
    catch (IndexOutOfBoundsException e) {
      return e.toString();
    }
  }

  private static boolean isSelected(WebElement element) {
    try {
      return element.isSelected();
    } catch (UnsupportedOperationException e) {
      return false;
    } catch (InvalidElementStateException e) {
      return false;
    }
  }

  public static String shortly(By selector) {
    if (selector instanceof By.ByCssSelector) {
      return selector.toString().replaceFirst("By\\.selector:\\s*(.*)", "$1");
    }
    return selector.toString();
  }

  public static String describeSubject(String step, 
      String prefix, String message, Condition condition) {
    return step + " " + prefix + condition + 
            (message != null ? " because " + message : "");
    
  }
  
  public static String selector(By selector) {
    return selector.toString().replaceFirst("By\\.selector:\\s*", "");
  }
}