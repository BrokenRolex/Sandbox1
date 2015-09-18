package hds.groovy

import java.util.regex.Matcher
import java.util.regex.Pattern

class PropertyInterpolator {
    static final Pattern INTERPOLATE_PATTERN = ~/(\$\{([^\$\{\}]+)\})/

    List interpolate (Properties properties) {
        List errors = []
        for (String key in properties.keySet()) {
            String val = properties[key] // property value to interpolate
            if (val?.contains('${')) {
                try {
                    String interpolatedValue = val
                    int matchAttempts = 0
                    while (true) {
                        Matcher matcher = interpolatedValue =~ INTERPOLATE_PATTERN
                        if (!matcher.find()) {
                            break // no match found, we must be done
                        }
                        String key1 = matcher.group(1) // full replacement var (e.g. ${var})
                        String key2 = matcher.group(2) // replacement var name (e.g. var)
                        String val2 = properties[key2] // replacement value
                        if (val2 == null) {
                            break // stop if a key is not found
                        }
                        interpolatedValue = interpolatedValue.replace(key1, val2)
                        properties[key] = interpolatedValue
                        if ((matchAttempts++) > 99) {
                            errors << "interpolation: possible circular reference detected in property [$key] value [$val]"
                            break // stop if infinite loop detected
                        }
                    }
                }
                catch (Exception e) {
                    errors << "cannot interpolate property [$key] value [$val] error [${e.message}]"
                }
            }
        }
        errors
    }

}
