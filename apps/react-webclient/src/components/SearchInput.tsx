import * as React from "react";
import { CircleX, Search } from "lucide-react";
import {
  InputGroup,
  InputGroupAddon,
  InputGroupButton,
  InputGroupInput,
} from "@/components/ui/input-group.tsx";
import { cn } from "@/lib/utils";

type SearchInputProps = {
  value: string;
  onChange: (value: string) => void;
  placeholder?: string;
  clearLabel: string;
  className?: string;
} & Omit<
  React.ComponentProps<"input">,
  "value" | "onChange" | "type" | "className"
>;

const SearchInput = React.forwardRef<HTMLInputElement, SearchInputProps>(
  (
    { value, onChange, placeholder, clearLabel, className, ...inputProps },
    ref,
  ) => {
    const inputRef = React.useRef<HTMLInputElement>(null);

    React.useImperativeHandle(ref, () => inputRef.current!);

    const handleClear = () => {
      onChange("");
      inputRef.current?.focus();
    };

    return (
      <InputGroup className={cn(className)}>
        <InputGroupAddon>
          <Search aria-hidden="true" />
        </InputGroupAddon>
        <InputGroupInput
          ref={inputRef}
          type="text"
          value={value}
          placeholder={placeholder}
          onChange={(event) => onChange(event.target.value)}
          {...inputProps}
        />
        {value.length > 0 && (
          <InputGroupAddon align="inline-end">
            <InputGroupButton
              size="icon-xs"
              aria-label={clearLabel}
              onClick={handleClear}
            >
              <CircleX />
            </InputGroupButton>
          </InputGroupAddon>
        )}
      </InputGroup>
    );
  },
);

SearchInput.displayName = "SearchInput";

export default SearchInput;
