import {Moon, Sun} from 'lucide-react'
import {Button} from "@/components/ui/button.tsx";;
import {useTheme} from "@/hooks/useTheme.tsx";

export function ModeToggle() {
    const {theme, toggleTheme} = useTheme();

    return <>
        <Button size={"icon"} variant={"ghost"} onClick={toggleTheme}>
            {theme === 'light' ? <Moon size={32}/> : <Sun size={32}/>}
        </Button>
    </>
}