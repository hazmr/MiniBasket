import { CircleCheck, Info } from "lucide-react";
import Link from "next/link";
import { Suspense } from "react";
import { login } from "@/actions/auth";
import { ActionForm } from "@/components/action-form";
import { AuthCard } from "@/components/auth-card";
import { SubmitButton } from "@/components/submit-button";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { Field, FieldGroup, FieldLabel } from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { single } from "@/lib/format";

const NOTICES: Record<string, string> = {
  verified: "Your email is verified. You can log in now.",
  reset: "Your password was changed. Log in with the new one.",
  expired: "Your session expired, please log in again.",
};

export default function LoginPage({ searchParams }: PageProps<"/login">) {
  return (
    <AuthCard
      title="Welcome back"
      description="Log in to your MiniBasket account."
      footer={
        <>
          <p>New here? <Link href="/register" className="font-medium text-primary hover:underline">Create an account</Link></p>
          <p><Link href="/forgot-password" className="font-medium text-primary hover:underline">Forgot your password?</Link></p>
        </>
      }
    >
      <Suspense>
        <LoginForm searchParams={searchParams} />
      </Suspense>
    </AuthCard>
  );
}

async function LoginForm({ searchParams }: { searchParams: PageProps<"/login">["searchParams"] }) {
  const params = await searchParams;
  const notice = Object.keys(NOTICES).find((key) => single(params[key]) === "1");

  return (
    <ActionForm action={login} className="flex flex-col gap-5">
      {notice && (
        <Alert className="border-primary/30 bg-accent text-accent-foreground">
          <CircleCheck />
          <AlertDescription className="text-accent-foreground">{NOTICES[notice]}</AlertDescription>
        </Alert>
      )}
      <input type="hidden" name="next" value={single(params.next) ?? "/"} />
      <FieldGroup className="gap-4">
        <Field>
          <FieldLabel htmlFor="email">Email</FieldLabel>
          <Input id="email" type="email" name="email" required autoComplete="email" placeholder="you@example.com" />
        </Field>
        <Field>
          <FieldLabel htmlFor="password">Password</FieldLabel>
          <Input id="password" type="password" name="password" required autoComplete="current-password" />
        </Field>
      </FieldGroup>
      <SubmitButton size="lg" className="w-full">Log in</SubmitButton>
      <Alert>
        <Info />
        <AlertDescription className="text-xs">
          Demo accounts: <span className="font-mono">customer@minibasket.dev</span> / Customer123! and{" "}
          <span className="font-mono">admin@minibasket.dev</span> / Admin123!
        </AlertDescription>
      </Alert>
    </ActionForm>
  );
}
