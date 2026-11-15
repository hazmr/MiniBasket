import { MailCheck } from "lucide-react";
import { Suspense } from "react";
import { verifyEmail } from "@/actions/auth";
import { ActionForm } from "@/components/action-form";
import { AuthCard } from "@/components/auth-card";
import { SubmitButton } from "@/components/submit-button";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import { Field, FieldLabel } from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { single } from "@/lib/format";

export default function VerifyEmailPage({ searchParams }: PageProps<"/verify-email">) {
  return (
    <AuthCard title="Verify your email" description="One last step before you can log in.">
      <Suspense>
        <VerifyForm searchParams={searchParams} />
      </Suspense>
    </AuthCard>
  );
}

async function VerifyForm({ searchParams }: { searchParams: PageProps<"/verify-email">["searchParams"] }) {
  const params = await searchParams;
  const token = single(params.token);

  // Opened from the link in the email: the token is already here, one click is enough
  if (token) {
    return (
      <ActionForm action={verifyEmail} className="flex flex-col gap-5">
        <p className="text-sm text-muted-foreground">Confirm that this email address is yours.</p>
        <input type="hidden" name="token" value={token} />
        <SubmitButton size="lg" className="w-full">Verify my email</SubmitButton>
      </ActionForm>
    );
  }

  return (
    <ActionForm action={verifyEmail} className="flex flex-col gap-5">
      {single(params.registered) === "1" && (
        <Alert className="border-primary/30 bg-accent text-accent-foreground">
          <MailCheck />
          <AlertTitle>Account created! Check your inbox.</AlertTitle>
          <AlertDescription className="text-accent-foreground/80">
            We sent you an email with a link to verify your address. Running the app locally? The email is
            in Mailpit at localhost:8026.
          </AlertDescription>
        </Alert>
      )}
      <Field>
        <FieldLabel htmlFor="token">Or paste the code from the email</FieldLabel>
        <Input id="token" name="token" required className="font-mono" />
      </Field>
      <SubmitButton size="lg" className="w-full">Verify email</SubmitButton>
    </ActionForm>
  );
}
