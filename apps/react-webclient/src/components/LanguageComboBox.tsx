import { useTranslation } from "react-i18next";
import {
  Select,
  SelectContent,
  SelectGroup,
  SelectItem,
  SelectTrigger,
} from "@/components/ui/select";
import ReactCountryFlag from "react-country-flag";

export function LanguageComboBox() {
  const { i18n } = useTranslation();
  const languages = Object.keys(i18n.store.data);

  const languageToCountry: Record<string, string> = {
    en: "GB",
    it: "IT",
  };

  const currentLanguage = i18n.resolvedLanguage ?? i18n.language ?? "en";

  return (
    <Select
      value={currentLanguage}
      onValueChange={(value) => {
        void i18n.changeLanguage(value);
        localStorage.setItem("language", value);
      }}
    >
      <SelectTrigger className="min-w-16 max-w-16">
        <div className="flex items-center gap-2">
          <ReactCountryFlag
            countryCode={languageToCountry[currentLanguage]}
            svg
            style={{ width: "1.25em", height: "1.25em" }}
            title={currentLanguage}
          />
          <span>{currentLanguage.toUpperCase()}</span>
        </div>
      </SelectTrigger>

      <SelectContent position={"popper"}>
        <SelectGroup>
          {languages.map((language) => (
            <SelectItem key={language} value={language}>
              <div className="flex items-center gap-2">
                <ReactCountryFlag
                  countryCode={languageToCountry[language]}
                  svg
                  style={{ width: "1.25em", height: "1.25em" }}
                  title={language}
                />
                <span>{language.toUpperCase()}</span>
              </div>
            </SelectItem>
          ))}
        </SelectGroup>
      </SelectContent>
    </Select>
  );
}
