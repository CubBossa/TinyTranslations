package de.cubbossa.translations.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import org.intellij.lang.annotations.RegExp;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class ComponentSplit {
  private ComponentSplit() {
  }

  /**
   * Splits a component and the underlying component tree by a regex.
   * Styles are being preserved, so splitting {@code <green>line1\nline2</green>} by the regex "\n" will produce
   * two components with {@link TextColor} green.
   * It matches the regex for all components and their content but not for a pattern that goes beyond one component.
   * {@code line1<green>ab</green>cline2} can therefore not be split with the regex "abc", because only "ab" or "c" would
   * match.
   *
   * @param self A component to split at a regex.
   * @param separator A regex to split the TextComponent content at.
   * @return A list of new components
   */
  @Contract(pure = true)
  public static @NotNull List<? extends Component> split(final @NotNull Component self, final @NotNull @RegExp String separator) {
    // First split component content
    List<Component> lines = splitComponentContent(self, separator);

    if (self.children().size() == 0) {
      return lines;
    }

    // Extract last split, which will contain all children of the same line
    Component parent = lines.remove(lines.size() - 1);

    // Process each child in order
    for (Component child : self.children()) {
      // Split child to List<Component>
      List<? extends Component> childSegments = split(child, separator);

      // each split will be a new row, except the first which will stick to the parent
      parent = parent.append(childSegments.get(0));
      for (int i = 1; i < childSegments.size(); i++) {
        lines.add(parent);
        parent = Component.empty().style(parent.style());
        parent = parent.append(childSegments.get(i));
      }
    }
    lines.add(parent);
    return lines;
  }

  /**
   * Splits a {@link TextComponent} by a regex.
   * @param component A {@link TextComponent} to split. If the provided Component is no instance of TextComponent, a
   *                  list with only the component is returned.
   * @param regex A regex that splits the content of the TextComponent, similar to {@link String#split(String)}
   * @return A list of TextComponents that contain the string segments of the original content.
   */
  private static List<Component> splitComponentContent(Component component, @RegExp String regex) {
    if (!(component instanceof TextComponent t)) {
      return List.of(component);
    }
    String[] segments = t.content().split(regex);
    if (segments.length == 0) {
      // Special case if the split regex is equals to the content.
      segments = new String[]{"", ""};
    }
    return Arrays.stream(segments)
        .map(s -> Component.text(s).style(t.style()))
        .map(c -> (Component) c)
        .collect(Collectors.toList());
  }
}
