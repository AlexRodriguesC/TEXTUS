import PathConstants from "@/routing/pathConstants.ts";

type LogoProps = {
    size?: 'sm' | 'md' | 'lg'
}

const sizeMap = {
    sm: 'h-6 w-6',
    md: 'h-8 w-8',
    lg: 'h-12 w-12',
}

function Logo({ size = 'md' }: LogoProps) {
    return (
        <a className="flex flex-row items-center gap-2" href={PathConstants.LOGIN}>
            <img src="/book.png" alt="logo" className={`${sizeMap[size]} object-contain`} />
            <div className="font-playfair font-bold">TEXTUS</div>
        </a>
    )
}

export default Logo