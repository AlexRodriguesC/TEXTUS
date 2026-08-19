import * as React from "react";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card.tsx";
import { Label } from "@/components/ui/label.tsx";
import { Input } from "@/components/ui/input.tsx";
import { Button } from "@/components/ui/button.tsx";
import { Eye, EyeClosed } from "lucide-react";
import { Link } from "@tanstack/react-router";
import CenteredPage from "@/components/CenteredPage.tsx";
import HomeLink from "@/components/HomeLink.tsx";

interface AuthFormCardProps {
  title: string;
  description: string;
  submitLabel: string;
  secondaryLink: { to: string; label: string };
  username: string;
  password: string;
  onUsernameChange: (value: string) => void;
  onPasswordChange: (value: string) => void;
  onSubmit: () => void;
}

function AuthFormCard({
  title,
  description,
  submitLabel,
  secondaryLink,
  username,
  password,
  onUsernameChange,
  onPasswordChange,
  onSubmit,
}: AuthFormCardProps) {
  const [isPasswordShown, setIsPasswordShown] = React.useState(false);

  return (
    <CenteredPage>
      <div className="flex w-full h-full justify-center items-center">
        <Card className="w-full max-w-sm">
          <CardHeader>
            <div className={"flex justify-end pt-0"}>
              <HomeLink />
            </div>
            <div>
              <CardTitle>{title}</CardTitle>
              <CardDescription>{description}</CardDescription>
            </div>
          </CardHeader>
          <CardContent>
            <form
              onSubmit={(e) => {
                e.preventDefault();
                onSubmit();
              }}
            >
              <div className="flex flex-col gap-8">
                <div className="flex flex-col gap-6">
                  <div className="grid gap-2">
                    <Label htmlFor="email">Email</Label>
                    <Input
                      id="email"
                      type="email"
                      placeholder="john.doe@textus.com"
                      value={username}
                      onChange={(e) => onUsernameChange(e.target.value)}
                      required
                    />
                  </div>
                  <div className="grid gap-2">
                    <Label htmlFor="password">Password</Label>
                    <div className="flex flex-row items-center relative">
                      <Input
                        id="password"
                        type={isPasswordShown ? "text" : "password"}
                        required
                        value={password}
                        onChange={(e) => onPasswordChange(e.target.value)}
                        className="pr-10"
                      />
                      <button
                        type="button"
                        onClick={() => setIsPasswordShown((prev) => !prev)}
                        className="absolute right-3 text-muted-foreground"
                      >
                        {isPasswordShown ? (
                          <Eye size={16} />
                        ) : (
                          <EyeClosed size={16} />
                        )}
                      </button>
                    </div>
                  </div>
                </div>
                <div className="flex flex-col gap-2">
                  <Button type="submit" className="w-full">
                    {submitLabel}
                  </Button>
                  <Button asChild variant="outline" className="w-full">
                    <Link to={secondaryLink.to}>{secondaryLink.label}</Link>
                  </Button>
                </div>
              </div>
            </form>
          </CardContent>
        </Card>
      </div>
    </CenteredPage>
  );
}

export default AuthFormCard;
