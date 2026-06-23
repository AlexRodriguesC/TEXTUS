import { useTranslation } from 'react-i18next'
import {
    Combobox,
    ComboboxContent,
    ComboboxInput,
    ComboboxEmpty,
    ComboboxList,
    ComboboxItem
} from "@/components/ui/combobox.tsx";

export function LanguageComboBox() {

    const { i18n, t } = useTranslation()
    const languages = i18n.languages

    return (
        <Combobox items={languages}>
            <ComboboxInput placeholder={t("language.combobox.placeholder")}/>
            <ComboboxContent>
                <ComboboxEmpty>{t("language.combobox.notfound")}</ComboboxEmpty>
                <ComboboxList>
                    {(i) => (
                        <ComboboxItem key={i} value={i}>
                            {i}
                        </ComboboxItem>
                    )}
                </ComboboxList>
            </ComboboxContent>
        </Combobox>
    )
}