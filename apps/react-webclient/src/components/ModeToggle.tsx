import {Moon, Sun} from 'lucide-react'
import * as React from "react";
import {Button} from "@/components/ui/button.tsx";

export function ModeToggle() {
    const [isDarkMode, setDarkMode] = React.useState(true);
    const onClick = () => setDarkMode(prev => !prev);


    return <>
        <Button variant={"ghost"} onClick={onClick}>
            {isDarkMode ? <Moon size={32}/> : <Sun size={32}/>}
        </Button>
    </>
}