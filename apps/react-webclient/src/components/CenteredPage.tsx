
function CenteredPage({ children }: { children: React.ReactNode }) {
    return (
        <div className="grid min-h-screen place-items-center px-4">
            {children}
        </div>
    );
}

export default CenteredPage;
