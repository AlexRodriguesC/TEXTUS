import { Button } from "@/components/ui/button.tsx";
import CenteredPage from "@/components/CenteredPage.tsx";
import IllustratedAside, {
  AsideFooter,
  AsideHeader,
} from "@/components/IllustratedAside.tsx";

interface SearchNotFoundAsideProps {
  message: string;
  resetLabel: string;
  onReset: () => void;
}

function SearchNotFoundAside({
  message,
  resetLabel,
  onReset,
}: SearchNotFoundAsideProps) {
  return (
    <div className="flex flex-1 min-h-0 items-center justify-center overflow-y-auto">
      <CenteredPage>
        <IllustratedAside
          className="gap-4"
          lottieSrc="/animations/not_found.lottie"
          lottieClassName="opacity-60"
          lottieProps={{ loop: true, autoplay: true }}
        >
          <AsideHeader>
            <p>{message}</p>
          </AsideHeader>
          <AsideFooter>
            <Button variant="outline" onClick={onReset}>
              {resetLabel}
            </Button>
          </AsideFooter>
        </IllustratedAside>
      </CenteredPage>
    </div>
  );
}

export default SearchNotFoundAside;
